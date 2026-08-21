package com.apihub.gateway.open;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 开放调用限流：QPS 固定窗口（1s）+ 每日配额。
 * <p>
 * 优先使用 Redis 计数器（支持多实例），Redis 不可用时降级为进程内近似计数。
 */
@Component
public class OpenApiRateLimiter {

    private static final String QPS_KEY_PREFIX = "apihub:qps:";
    private static final String QUOTA_KEY_PREFIX = "apihub:quota:";
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RedisTemplate<String, String> redisTemplate;
    private final boolean enabled;
    private final InMemoryRateLimiter fallback = new InMemoryRateLimiter();

    public OpenApiRateLimiter(
            RedisTemplate<String, String> redisTemplate,
            @Value("${apihub.open.rate-limit.enabled:true}") boolean enabled
    ) {
        this.redisTemplate = redisTemplate;
        this.enabled = enabled;
    }

    /**
     * @return true 放行；false 超限（对应 QUOTA_EXCEEDED）
     */
    public boolean tryAcquire(String appId, int qpsLimit, int dailyQuota) {
        if (!enabled || qpsLimit <= 0 || dailyQuota <= 0) {
            return true;
        }
        try {
            // QPS：固定 1 秒窗口
            String qpsKey = QPS_KEY_PREFIX + appId + ":" + (System.currentTimeMillis() / 1000);
            Long qps = redisTemplate.opsForValue().increment(qpsKey);
            if (qps != null && qps == 1) {
                redisTemplate.expire(qpsKey, Duration.ofSeconds(2));
            }
            if (qps != null && qps > qpsLimit) {
                return false;
            }
            // 日配额：按自然日计数
            String quotaKey = QUOTA_KEY_PREFIX + appId + ":" + LocalDate.now().format(DAY);
            Long used = redisTemplate.opsForValue().increment(quotaKey);
            if (used != null && used == 1) {
                long remainSeconds = Duration.between(
                        LocalDateTime.now(),
                        LocalDate.now().plusDays(1).atStartOfDay()
                ).getSeconds();
                redisTemplate.expire(quotaKey, Duration.ofSeconds(Math.max(remainSeconds, 60)));
            }
            return used == null || used <= dailyQuota;
        } catch (Exception ex) {
            return fallback.tryAcquire(appId, qpsLimit, dailyQuota);
        }
    }

    /** 进程内近似限流（单实例演示降级） */
    static final class InMemoryRateLimiter {

        private final ConcurrentHashMap<String, AtomicInteger> qpsCounters = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, AtomicInteger> quotaCounters = new ConcurrentHashMap<>();

        boolean tryAcquire(String appId, int qpsLimit, int dailyQuota) {
            long second = System.currentTimeMillis() / 1000;
            String qpsKey = appId + ":" + second;
            AtomicInteger qps = qpsCounters.computeIfAbsent(qpsKey, k -> new AtomicInteger());
            if (qps.incrementAndGet() > qpsLimit) {
                return false;
            }
            if (qpsCounters.size() > 10_000) {
                qpsCounters.keySet().removeIf(k -> !k.endsWith(":" + second));
            }
            String quotaKey = appId + ":" + LocalDate.now().format(DAY);
            AtomicInteger quota = quotaCounters.computeIfAbsent(quotaKey, k -> new AtomicInteger());
            if (quota.incrementAndGet() > dailyQuota) {
                return false;
            }
            if (quotaCounters.size() > 10_000) {
                quotaCounters.clear();
            }
            return true;
        }
    }
}
