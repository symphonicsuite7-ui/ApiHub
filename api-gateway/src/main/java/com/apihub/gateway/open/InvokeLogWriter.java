package com.apihub.gateway.open;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 调用日志异步写入器（最终版）。
 * <p>
 * 网关转发完成后将调用信息异步写入 api_invoke_log：
 * <ul>
 *   <li>可配置线程池（core/max/queue），队列满时丢弃并告警，绝不阻塞主链路；</li>
 *   <li>开放调用解析 interface_id（经 OpenAppRepository 带缓存查询）；</li>
 *   <li>写失败仅记 warn；{@link #shutdown()} 优雅关闭。</li>
 * </ul>
 */
@Component
public class InvokeLogWriter {

    private static final Logger log = LoggerFactory.getLogger(InvokeLogWriter.class);

    private static final String INSERT_SQL =
            "INSERT INTO api_invoke_log (trace_id, app_id, interface_id, request_path, method, status_code, cost_ms, ip) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;
    private final OpenAppRepository openAppRepository;
    private final ThreadPoolExecutor executor;

    public InvokeLogWriter(
            JdbcTemplate jdbcTemplate,
            OpenAppRepository openAppRepository,
            @Value("${apihub.open.log.core-pool-size:1}") int corePoolSize,
            @Value("${apihub.open.log.max-pool-size:2}") int maxPoolSize,
            @Value("${apihub.open.log.queue-capacity:1000}") int queueCapacity
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.openAppRepository = openAppRepository;
        this.executor = new ThreadPoolExecutor(
                Math.max(corePoolSize, 1),
                Math.max(maxPoolSize, corePoolSize),
                60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(Math.max(queueCapacity, 1)),
                r -> {
                    Thread thread = new Thread(r, "invoke-log-writer");
                    thread.setDaemon(true);
                    return thread;
                },
                (runnable, pool) -> log.warn("调用日志队列已满，丢弃一条日志")
        );
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
            Long interfaceId = StringUtils.hasText(appId)
                    ? openAppRepository.findInterfaceId(path, method)
                    : null;
            jdbcTemplate.update(INSERT_SQL,
                    traceId, appId, interfaceId, path, method, statusCode, costMs, ip);
        } catch (Exception ex) {
            log.warn("调用日志写入失败: traceId={}, path={}, err={}", traceId, path, ex.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
