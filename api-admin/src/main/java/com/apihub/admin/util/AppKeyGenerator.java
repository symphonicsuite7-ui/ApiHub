package com.apihub.admin.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * 开放应用密钥生成器。
 * <p>
 * AppId 形如 {@code app_<时间戳hex><8位随机>}，保证可读且全局唯一（数据库唯一键兜底）；
 * AppSecret 为 24 字节安全随机数的 Base64URL 编码，用于开放调用 HMAC 签名。
 */
public final class AppKeyGenerator {

    private static final String APP_ID_PREFIX = "app_";
    private static final SecureRandom RANDOM = new SecureRandom();

    private AppKeyGenerator() {
    }

    public static String generateAppId() {
        String timeHex = Long.toHexString(System.currentTimeMillis());
        String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return APP_ID_PREFIX + timeHex + rand;
    }

    public static String generateAppSecret() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
