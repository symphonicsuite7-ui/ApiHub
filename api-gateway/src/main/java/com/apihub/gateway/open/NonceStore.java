package com.apihub.gateway.open;

/**
 * Nonce 防重放存储。同一 nonce 在 ttlMillis 内只能消费一次。
 */
public interface NonceStore {

    /**
     * @return true 表示首次消费成功；false 表示该 nonce 已存在（重放）
     */
    boolean tryConsume(String nonce, long ttlMillis);
}
