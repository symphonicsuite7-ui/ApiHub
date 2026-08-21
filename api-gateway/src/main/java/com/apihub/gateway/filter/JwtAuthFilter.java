package com.apihub.gateway.filter;

import com.apihub.common.constant.ApiHeaders;
import com.apihub.common.constant.RequestAttrs;
import com.apihub.common.jwt.JwtUtil;
import com.apihub.common.result.ErrorCode;
import com.apihub.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理端接口校验 JWT；登录注册、开放演示接口放行。
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter implements Ordered {

    private static final List<String> WHITE_PREFIXES = List.of(
            "/health",
            "/web/",
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/health",
            "/api/open/"
    );

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isWhite(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        String authorization = request.getHeader(ApiHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response);
            return;
        }
        try {
            Claims claims = jwtUtil.parse(authorization.substring(7).trim());
            // 解析用户身份，供后续过滤器透传给下游服务
            request.setAttribute(RequestAttrs.USER_ID, jwtUtil.getUserId(claims));
            Object username = claims.get("username");
            request.setAttribute(RequestAttrs.USER_NAME, username == null ? "" : String.valueOf(username));
            Object roles = claims.get("roles");
            request.setAttribute(RequestAttrs.USER_ROLES, roles == null ? ""
                    : ((List<?>) roles).stream().map(String::valueOf).collect(Collectors.joining(",")));
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException ex) {
            writeUnauthorized(response);
        }
    }

    private boolean isWhite(String uri) {
        if (uri == null) {
            return false;
        }
        for (String prefix : WHITE_PREFIXES) {
            if (uri.equals(prefix) || uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Result<Void> body = Result.<Void>fail(ErrorCode.UNAUTHORIZED).traceId(MDC.get("traceId"));
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
