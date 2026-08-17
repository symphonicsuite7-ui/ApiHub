package com.apihub.gateway.filter;

import com.apihub.common.constant.ApiHeaders;
import com.apihub.common.result.ErrorCode;
import com.apihub.common.result.Result;
import com.apihub.gateway.config.RouteProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

/**
 * 将 /api/auth、/api/admin、/api/open 转发到对应下游服务。
 */
@Component
public class GatewayProxyFilter extends OncePerRequestFilter implements Ordered {

    private static final Set<String> SKIP_HEADERS = Set.of(
            "host", "connection", "content-length", "transfer-encoding", "accept-encoding"
    );

    private final RouteProperties routeProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GatewayProxyFilter(RouteProperties routeProperties, RestTemplate restTemplate) {
        this.routeProperties = routeProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String targetBase = resolveTarget(uri);
        if (targetBase == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String targetPath = uri.substring("/api".length());
        String query = request.getQueryString();
        String url = targetBase + targetPath + (query == null ? "" : "?" + query);

        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(name -> {
            if (!SKIP_HEADERS.contains(name.toLowerCase())) {
                headers.put(name, Collections.list(request.getHeaders(name)));
            }
        });
        String traceId = MDC.get("traceId");
        if (traceId != null) {
            headers.set(ApiHeaders.TRACE_ID, traceId);
        }

        byte[] body = request.getInputStream().readAllBytes();
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        HttpEntity<byte[]> entity = new HttpEntity<>(body.length == 0 ? null : body, headers);
        try {
            ResponseEntity<byte[]> downstream = restTemplate.exchange(url, method, entity, byte[].class);
            response.setStatus(downstream.getStatusCode().value());
            downstream.getHeaders().forEach((key, values) -> {
                if (!SKIP_HEADERS.contains(key.toLowerCase()) && !"content-encoding".equalsIgnoreCase(key)) {
                    for (String value : values) {
                        response.addHeader(key, value);
                    }
                }
            });
            if (traceId != null) {
                response.setHeader(ApiHeaders.TRACE_ID, traceId);
            }
            byte[] respBody = downstream.getBody();
            if (respBody != null && respBody.length > 0) {
                response.getOutputStream().write(respBody);
            }
        } catch (ResourceAccessException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Result<Void> fail = Result.<Void>fail(ErrorCode.INTERNAL_ERROR.getCode(), "下游服务不可用: " + url)
                    .traceId(traceId);
            response.getWriter().write(objectMapper.writeValueAsString(fail));
        }
    }

    private String resolveTarget(String uri) {
        if (uri == null) {
            return null;
        }
        if (uri.startsWith("/api/auth")) {
            return routeProperties.getAuth();
        }
        if (uri.startsWith("/api/admin")) {
            return routeProperties.getAdmin();
        }
        if (uri.startsWith("/api/open")) {
            return routeProperties.getInvoke();
        }
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 30;
    }
}
