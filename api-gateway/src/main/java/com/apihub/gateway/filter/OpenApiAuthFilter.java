package com.apihub.gateway.filter;

import com.apihub.common.constant.ApiHeaders;
import com.apihub.common.constant.RequestAttrs;
import com.apihub.common.result.ErrorCode;
import com.apihub.common.result.Result;
import com.apihub.gateway.open.AppInfo;
import com.apihub.gateway.open.NonceStore;
import com.apihub.gateway.open.OpenApiRateLimiter;
import com.apihub.gateway.open.OpenAppRepository;
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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 开放调用签名鉴权过滤器。
 * <p>
 * 对 /api/open/**（健康检查除外）依次校验：
 * 1. X-App-Id / X-Timestamp / X-Nonce / X-Sign 四个头齐全（400）
 * 2. 应用存在且启用（401/10003）
 * 3. 时间戳在窗口内，默认 ±300s（401/10005）
 * 4. 签名正确：HMAC-SHA256(secret, appId+timestamp+nonce+body)，常量时间比较（401/10004）
 * 5. Nonce 未重放，Redis 存储 + 内存降级（401/10006）
 * 6. QPS / 日配额限流（429/10009）
 * 7. 接口已开通且上线（403/10008）
 * <p>
 * 通过后将归属用户写入 Request Attribute，由 GatewayProxyFilter 透传。
 */
@Component
public class OpenApiAuthFilter extends OncePerRequestFilter implements Ordered {

    private static final Set<String> WHITE_PATHS = Set.of("/api/open/health");

    private final OpenAppRepository appRepository;
    private final NonceStore nonceStore;
    private final OpenApiRateLimiter rateLimiter;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final long timestampWindowMillis;

    public OpenApiAuthFilter(
            OpenAppRepository appRepository,
            NonceStore nonceStore,
            OpenApiRateLimiter rateLimiter,
            @Value("${apihub.open.timestamp-window-seconds:300}") long timestampWindowSeconds
    ) {
        this.appRepository = appRepository;
        this.nonceStore = nonceStore;
        this.rateLimiter = rateLimiter;
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
            writeError(response, ErrorCode.BAD_REQUEST.getCode(), "缺少开放调用请求头",
                    HttpServletResponse.SC_BAD_REQUEST, traceId);
            return;
        }
        appId = appId.trim();
        timestamp = timestamp.trim();
        nonce = nonce.trim();
        sign = sign.trim();

        // 1. 应用存在且启用
        AppInfo app = appRepository.findByAppId(appId);
        if (app == null || app.status() != 1) {
            writeError(response, ErrorCode.APP_INVALID, HttpServletResponse.SC_UNAUTHORIZED, traceId);
            return;
        }

        // 2. 时间戳窗口
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException ex) {
            writeError(response, ErrorCode.TIMESTAMP_INVALID, HttpServletResponse.SC_UNAUTHORIZED, traceId);
            return;
        }
        if (Math.abs(System.currentTimeMillis() - ts) > timestampWindowMillis) {
            writeError(response, ErrorCode.TIMESTAMP_INVALID, HttpServletResponse.SC_UNAUTHORIZED, traceId);
            return;
        }

        // 3. 验签（先验签再消费 nonce，避免无效请求消耗合法 nonce）
        String content = SignatureUtil.buildSignContent(appId, timestamp, nonce, body);
        String expected = SignatureUtil.hmacSha256Hex(app.appSecret(), content);
        if (!SignatureUtil.constantTimeEquals(expected, sign)) {
            writeError(response, ErrorCode.SIGN_INVALID, HttpServletResponse.SC_UNAUTHORIZED, traceId);
            return;
        }

        // 4. Nonce 防重放
        if (!nonceStore.tryConsume(nonce, timestampWindowMillis)) {
            writeError(response, ErrorCode.NONCE_REPLAY, HttpServletResponse.SC_UNAUTHORIZED, traceId);
            return;
        }

        // 5. QPS / 日配额限流
        if (!rateLimiter.tryAcquire(appId, app.qpsLimit(), app.dailyQuota())) {
            writeError(response, ErrorCode.QUOTA_EXCEEDED, 429, traceId);
            return;
        }

        // 6. 接口开通且上线
        if (!appRepository.isGranted(appId, uri, request.getMethod())) {
            writeError(response, ErrorCode.INTERFACE_NOT_GRANTED, HttpServletResponse.SC_FORBIDDEN, traceId);
            return;
        }

        // 校验通过：透传归属用户身份
        wrapped.setAttribute(RequestAttrs.USER_ID, String.valueOf(app.userId()));
        wrapped.setAttribute(RequestAttrs.OPEN_APP_ID, appId);
        filterChain.doFilter(wrapped, response);
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
}
