package com.apihub.gateway.open;

import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis Nonce 存储：SET NX EX 原子消费，天然支持多实例网关。
 * Redis 不可用时自动降级到内存实现，保证演示环境可用。
 */
@Primary
@Component
public class RedisNonceStore implements NonceStore {

    private static final String KEY_PREFIX = "apihub:nonce:";

    private final RedisTemplate<String, String> redisTemplate;
    private final InMemoryNonceStore fallback = new InMemoryNonceStore();

    public RedisNonceStore(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryConsume(String nonce, long ttlMillis) {
        try {
            Boolean ok = redisTemplate.opsForValue()
                    .setIfAbsent(KEY_PREFIX + nonce, "1", Duration.ofMillis(ttlMillis));
            return Boolean.TRUE.equals(ok);
        } catch (Exception ex) {
            // Redis 连接失败：降级内存，保证请求不中断
            return fallback.tryConsume(nonce, ttlMillis);
        }
    }
}
