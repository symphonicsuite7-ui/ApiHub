package com.apihub.gateway.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * 开放调用签名工具。
 * <p>
 * 签名串约定：{@code appId + timestamp + nonce + body}（body 为空串时省略内容）。
 * 算法：HMAC-SHA256(secret, 签名串)，结果 hex 小写放入 X-Sign。
 */
public final class SignatureUtil {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private SignatureUtil() {
    }

    public static String hmacSha256Hex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (Exception ex) {
            throw new IllegalStateException("HMAC-SHA256 计算失败", ex);
        }
    }

    /** 常量时间比较，防时序攻击 */
    public static boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8)
        );
    }

    /** 按约定拼接签名内容 */
    public static String buildSignContent(String appId, String timestamp, String nonce, byte[] body) {
        String bodyPart = (body == null || body.length == 0) ? "" : new String(body, StandardCharsets.UTF_8);
        return appId + timestamp + nonce + bodyPart;
    }
}
