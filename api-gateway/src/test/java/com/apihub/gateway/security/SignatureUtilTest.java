package com.apihub.gateway.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignatureUtilTest {

    @Test
    void hmacSha256Hex_matchesKnownVector() {
        // HMAC-SHA256("secret", "hello") 已知值（由独立实现验证），用于校验算法实现正确
        String expect = "88aab3ede8d3adf94d26ab90d3bafd4a2083070c3bcce9c014ee04a443847c0b";
        assertTrue(SignatureUtil.constantTimeEquals(expect, SignatureUtil.hmacSha256Hex("secret", "hello")));
    }

    @Test
    void buildSignContent_appendsBodyWhenPresent() {
        String content = SignatureUtil.buildSignContent("app_1", "1700000000000", "abc", "{\"q\":1}".getBytes());
        assertTrue(content.endsWith("{\"q\":1}"));
        assertTrue(content.startsWith("app_11700000000000abc"));
    }

    @Test
    void buildSignContent_emptyBodyIsSkipped() {
        String content = SignatureUtil.buildSignContent("app_1", "1700000000000", "abc", new byte[0]);
        assertTrue(content.equals("app_11700000000000abc"));
    }

    @Test
    void constantTimeEquals_rejectsDifferentAndNull() {
        assertTrue(SignatureUtil.constantTimeEquals("abc", "abc"));
        assertFalse(SignatureUtil.constantTimeEquals("abc", "abd"));
        assertFalse(SignatureUtil.constantTimeEquals("abc", null));
        assertFalse(SignatureUtil.constantTimeEquals(null, "abc"));
    }
}
