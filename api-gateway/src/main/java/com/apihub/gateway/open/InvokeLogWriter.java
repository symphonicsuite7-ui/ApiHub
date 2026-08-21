package com.apihub.gateway.open;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 调用日志异步写入器（v1）。
 * <p>
 * 网关转发完成后将调用信息异步写入 api_invoke_log，写失败仅记 warn，
 * 绝不影响主请求链路。线程为 daemon，进程退出自动终止。
 */
@Component
public class InvokeLogWriter {

    private static final Logger log = LoggerFactory.getLogger(InvokeLogWriter.class);

    private final JdbcTemplate jdbcTemplate;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "invoke-log-writer");
        thread.setDaemon(true);
        return thread;
    });

    public InvokeLogWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 异步写入一条调用日志。
     *
     * @param traceId    全链路追踪号
     * @param appId      开放调用方应用标识；管理端调用为 null
     * @param path       请求路径（含 /api 前缀）
     * @param method     HTTP 方法
     * @param statusCode 响应状态码
     * @param costMs     耗时毫秒
     * @param ip         客户端 IP
     */
    public void writeAsync(String traceId, String appId, String path, String method,
                           int statusCode, long costMs, String ip) {
        executor.submit(() -> write(traceId, appId, path, method, statusCode, costMs, ip));
    }

    /** 同步写库；供异步线程与单元测试调用。 */
    void write(String traceId, String appId, String path, String method,
               int statusCode, long costMs, String ip) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO api_invoke_log (trace_id, app_id, request_path, method, status_code, cost_ms, ip) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    traceId, appId, path, method, statusCode, costMs, ip);
        } catch (Exception ex) {
            log.warn("调用日志写入失败: traceId={}, path={}, err={}", traceId, path, ex.getMessage());
        }
    }
}
