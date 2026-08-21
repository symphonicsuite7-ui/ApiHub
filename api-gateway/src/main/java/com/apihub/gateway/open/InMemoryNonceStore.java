package com.apihub.gateway.open;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存 Nonce 存储：单实例网关演示场景使用，进程重启后失效可接受。
 * 惰性清理过期项，避免无限增长。
 */
@Component
public class InMemoryNonceStore implements NonceStore {

    private static final int MAX_ENTRIES = 50_000;

    private final ConcurrentHashMap<String, Long> nonces = new ConcurrentHashMap<>();

    @Override
    public boolean tryConsume(String nonce, long ttlMillis) {
        long now = System.currentTimeMillis();
        Long expireAt = nonces.get(nonce);
        if (expireAt != null && expireAt > now) {
            return false;
        }
        if (nonces.size() > MAX_ENTRIES) {
            nonces.entrySet().removeIf(e -> e.getValue() <= now);
        }
        nonces.put(nonce, now + ttlMillis);
        return true;
    }
}
