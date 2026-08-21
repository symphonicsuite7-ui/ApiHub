package com.apihub.gateway.filter;

import com.apihub.common.constant.ApiHeaders;
import com.apihub.common.constant.RequestAttrs;
import com.apihub.common.result.ErrorCode;
import com.apihub.common.result.Result;
import com.apihub.gateway.security.SignatureUtil;
import com.apihub.gateway.web.CachedBodyHttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 开放调用签名鉴权过滤器（v1 简单版）。
 * <p>
 * 对 /api/open/**（健康检查除外）校验：
 * 1. X-App-Id / X-Timestamp / X-Nonce / X-Sign 四个头齐全
 * 2. 应用存在且启用（查 api_app）
 * 3. 时间戳在窗口内（默认 ±300s）
 * 4. 签名正确：HMAC-SHA256(secret, appId+timestamp+nonce+body)
 * 5. Nonce 未重放（内存缓存，TTL=时间窗）
 * 6. 接口已开通且上线（api_app_interface JOIN api_interface）
 * <p>
 * 校验通过后将归属用户写入 Request Attribute，由 GatewayProxyFilter 透传。
 */
@Component
public class OpenApiAuthFilter extends OncePerRequestFilter implements Ordered {

    private static final Set<String> WHITE_PATHS = Set.of("/api/open/health");
    private static final int NONCE_CACHE_MAX = 10000;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final long timestampWindowMillis;

    /** nonce -> 过期时间戳(ms)，防止重放 */
    private final ConcurrentHashMap<String, Long> nonceCache = new ConcurrentHashMap<>();

    public OpenApiAuthFilter(
            JdbcTemplate jdbcTemplate,
            @Value("${apihub.open.timestamp-window-seconds:300}") long timestampWindowSeconds
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.timestampWindowMillis = timestampWindowSeconds * 1000L;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/open/") || "OPTIONS".equalsIgnoreCase(request.getMethod())
                || WHITE_PATHS.contains(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String traceId = MDC.get("traceId");
        byte[] body = request.getInputStream().readAllBytes();
        CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request, body);

        String appId = request.getHeader(ApiHeaders.APP_ID);
        String timestamp = request.getHeader(ApiHeaders.TIMESTAMP);
        String nonce = request.getHeader(ApiHeaders.NONCE);
        String sign = request.getHeader(ApiHeaders.SIGN);
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(timestamp)
                || !StringUtils.hasText(nonce) || !StringUtils.hasText(sign)) {
            writeError(response, ErrorCode.BAD_REQUEST.getCode(), "缺少开放调用请求头", HttpServletResponse.SC_BAD_REQUEST, traceId);
            return;
        }

        // 1. 应用存在且启用
        AppRow app = findApp(appId);
        if (app == null || app.status != 1) {
            writeError(response, ErrorCode.APP_INVALID, HttpServletResponse.SC_UNAUTHORIZED, traceId);
            return;
        }

        // 2. 时间戳窗口
        long ts;
        try {
            ts = Long.parseLong(timestamp.trim());
        } catch (NumberFormatException ex) {
            writeError(response, ErrorCode.TIMESTAMP_INVALID, HttpServletResponse.SC_UNAUTHORIZED, traceId);
            return;
        }
        if (Math.abs(System.currentTimeMillis() - ts) > timestampWindowMillis) {
            writeError(response, ErrorCode.TIMESTAMP_INVALID, HttpServletResponse.SC_UNAUTHORIZED, traceId);
            return;
        }

        // 3. 验签（先验签再消费 nonce，避免无效请求消耗合法 nonce）
        String content = SignatureUtil.buildSignContent(appId.trim(), timestamp.trim(), nonce.trim(), body);
        String expected = SignatureUtil.hmacSha256Hex(app.appSecret, content);
        if (!SignatureUtil.constantTimeEquals(expected, sign.trim())) {
            writeError(response, ErrorCode.SIGN_INVALID, HttpServletResponse.SC_UNAUTHORIZED, traceId);
            return;
        }

        // 4. Nonce 防重放
        if (!tryConsumeNonce(nonce.trim())) {
            writeError(response, ErrorCode.NONCE_REPLAY, HttpServletResponse.SC_UNAUTHORIZED, traceId);
            return;
        }

        // 5. 接口开通且上线
        if (!isGranted(appId.trim(), uri, request.getMethod())) {
            writeError(response, ErrorCode.INTERFACE_NOT_GRANTED, HttpServletResponse.SC_FORBIDDEN, traceId);
            return;
        }

        // 校验通过：透传归属用户身份
        wrapped.setAttribute(RequestAttrs.USER_ID, String.valueOf(app.userId));
        wrapped.setAttribute(RequestAttrs.OPEN_APP_ID, appId.trim());
        filterChain.doFilter(wrapped, response);
    }

    private AppRow findApp(String appId) {
        return jdbcTemplate.query(
                "SELECT id, app_id, app_secret, user_id, status FROM api_app WHERE app_id = ?",
                rs -> rs.next()
                        ? new AppRow(rs.getLong("id"), rs.getString("app_id"), rs.getString("app_secret"),
                        rs.getLong("user_id"), rs.getInt("status"))
                        : null,
                appId
        );
    }

    private boolean isGranted(String appId, String path, String method) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM api_app_interface a JOIN api_interface i ON a.interface_id = i.id "
                        + "WHERE a.app_id = ? AND i.path = ? AND i.method = ? AND i.status = 1",
                Integer.class, appId, path, method.toUpperCase()
        );
        return count != null && count > 0;
    }

    private boolean tryConsumeNonce(String nonce) {
        long now = System.currentTimeMillis();
        Long expireAt = nonceCache.get(nonce);
        if (expireAt != null && expireAt > now) {
            return false;
        }
        if (nonceCache.size() > NONCE_CACHE_MAX) {
            nonceCache.entrySet().removeIf(e -> e.getValue() <= now);
        }
        nonceCache.put(nonce, now + timestampWindowMillis);
        return true;
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode, int httpStatus, String traceId)
            throws IOException {
        writeError(response, errorCode.getCode(), errorCode.getMsg(), httpStatus, traceId);
    }

    private void writeError(HttpServletResponse response, int code, String msg, int httpStatus, String traceId)
            throws IOException {
        response.setStatus(httpStatus);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Result<Void> body = Result.<Void>fail(code, msg).traceId(traceId);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 25;
    }

    /** 应用查询结果（v1 简单行对象） */
    private static final class AppRow {
        final long id;
        final String appId;
        final String appSecret;
        final long userId;
        final int status;

        AppRow(long id, String appId, String appSecret, long userId, int status) {
            this.id = id;
            this.appId = appId;
            this.appSecret = appSecret;
            this.userId = userId;
            this.status = status;
        }
    }
}
