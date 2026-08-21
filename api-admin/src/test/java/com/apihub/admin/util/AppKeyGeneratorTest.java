package com.apihub.admin.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppKeyGeneratorTest {

    @Test
    void generateAppId_hasPrefixAndUnique() {
        String appId = AppKeyGenerator.generateAppId();
        assertTrue(appId.startsWith("app_"), "AppId 应以 app_ 开头");
        assertTrue(appId.length() >= 16, "AppId 应包含时间戳与随机段");

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(AppKeyGenerator.generateAppId());
        }
        assertEquals(1000, ids.size(), "批量生成的 AppId 不应重复");
    }

    @Test
    void generateAppSecret_urlSafeBase64WithoutPadding() {
        String secret = AppKeyGenerator.generateAppSecret();
        // 24 字节 Base64URL 无填充 => 32 字符
        assertEquals(32, secret.length());
        assertTrue(secret.matches("[A-Za-z0-9_-]{32}"), "Secret 应只包含 URL 安全字符");
    }

    @Test
    void generateAppSecret_randomOnEachCall() {
        assertNotEquals(AppKeyGenerator.generateAppSecret(), AppKeyGenerator.generateAppSecret());
    }
}
