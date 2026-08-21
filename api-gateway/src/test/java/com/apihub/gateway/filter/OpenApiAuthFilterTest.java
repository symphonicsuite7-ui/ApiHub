package com.apihub.gateway.filter;

import com.apihub.common.constant.ApiHeaders;
import com.apihub.common.constant.RequestAttrs;
import com.apihub.common.result.Result;
import com.apihub.gateway.open.AppInfo;
import com.apihub.gateway.open.NonceStore;
import com.apihub.gateway.open.OpenApiRateLimiter;
import com.apihub.gateway.open.OpenAppRepository;
import com.apihub.gateway.security.SignatureUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OpenApiAuthFilter 全场景测试：缺头/应用无效/时间窗/签名/重放/限流/未开通/成功。
 */
class OpenApiAuthFilterTest {

    private static final String APP_ID = "app_test";
    private static final String SECRET = "test-secret";
    private static final String PATH = "/api/open/weather";

    private OpenAppRepository appRepository;
    private NonceStore nonceStore;
    private OpenApiRateLimiter rateLimiter;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        appRepository = mock(OpenAppRepository.class);
        nonceStore = mock(NonceStore.class);
        rateLimiter = mock(OpenApiRateLimiter.class);
        OpenApiAuthFilter filter = new OpenApiAuthFilter(appRepository, nonceStore, rateLimiter, 300L);
        mvc = MockMvcBuilders.standaloneSetup(new StubController())
                .addFilters(filter)
                .build();

        AppInfo app = new AppInfo(1L, APP_ID, SECRET, 42L, 1, 10, 1000);
        when(appRepository.findByAppId(APP_ID)).thenReturn(app);
        when(appRepository.isGranted(APP_ID, PATH, "GET")).thenReturn(true);
        when(nonceStore.tryConsume(anyString(), anyLong())).thenReturn(true);
        when(rateLimiter.tryAcquire(anyString(), anyInt(), anyInt())).thenReturn(true);
    }

    private String sign(long timestamp, String nonce, String body) {
        String content = SignatureUtil.buildSignContent(APP_ID, String.valueOf(timestamp), nonce, body.getBytes());
        return SignatureUtil.hmacSha256Hex(SECRET, content);
    }

    @Test
    void healthEndpointIsWhitelisted() throws Exception {
        mvc.perform(get("/api/open/health")).andExpect(status().isOk());
    }

    @Test
    void missingHeadersRejectedWith400() throws Exception {
        mvc.perform(get(PATH))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    JsonNode json = readBody(result);
                    assertEquals(400, json.get("code").asInt());
                });
    }

    @Test
    void unknownAppRejectedWithAppInvalid() throws Exception {
        long ts = System.currentTimeMillis();
        String nonce = "n1";
        mvc.perform(get(PATH)
                        .header(ApiHeaders.APP_ID, "app_not_exist")
                        .header(ApiHeaders.TIMESTAMP, ts)
                        .header(ApiHeaders.NONCE, nonce)
                        .header(ApiHeaders.SIGN, sign(ts, nonce, "")))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> {
                    JsonNode json = readBody(result);
                    assertEquals(10003, json.get("code").asInt());
                });
    }

    @Test
    void expiredTimestampRejected() throws Exception {
        long ts = System.currentTimeMillis() - 600_000; // 10 分钟前，超出 ±300s 窗口
        String nonce = "n2";
        mvc.perform(get(PATH)
                        .header(ApiHeaders.APP_ID, APP_ID)
                        .header(ApiHeaders.TIMESTAMP, ts)
                        .header(ApiHeaders.NONCE, nonce)
                        .header(ApiHeaders.SIGN, sign(ts, nonce, "")))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> {
                    JsonNode json = readBody(result);
                    assertEquals(10005, json.get("code").asInt());
                });
    }

    @Test
    void badSignRejected() throws Exception {
        long ts = System.currentTimeMillis();
        String nonce = "n3";
        mvc.perform(get(PATH)
                        .header(ApiHeaders.APP_ID, APP_ID)
                        .header(ApiHeaders.TIMESTAMP, ts)
                        .header(ApiHeaders.NONCE, nonce)
                        .header(ApiHeaders.SIGN, "deadbeef"))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> {
                    JsonNode json = readBody(result);
                    assertEquals(10004, json.get("code").asInt());
                });
    }

    @Test
    void replayNonceRejected() throws Exception {
        long ts = System.currentTimeMillis();
        String nonce = "n4";
        when(nonceStore.tryConsume(nonce, 300_000L)).thenReturn(false);
        mvc.perform(get(PATH)
                        .header(ApiHeaders.APP_ID, APP_ID)
                        .header(ApiHeaders.TIMESTAMP, ts)
                        .header(ApiHeaders.NONCE, nonce)
                        .header(ApiHeaders.SIGN, sign(ts, nonce, "")))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> {
                    JsonNode json = readBody(result);
                    assertEquals(10006, json.get("code").asInt());
                });
    }

    @Test
    void rateLimitRejectedWith429() throws Exception {
        long ts = System.currentTimeMillis();
        String nonce = "n5";
        when(rateLimiter.tryAcquire(anyString(), anyInt(), anyInt())).thenReturn(false);
        mvc.perform(get(PATH)
                        .header(ApiHeaders.APP_ID, APP_ID)
                        .header(ApiHeaders.TIMESTAMP, ts)
                        .header(ApiHeaders.NONCE, nonce)
                        .header(ApiHeaders.SIGN, sign(ts, nonce, "")))
                .andExpect(status().isTooManyRequests())
                .andExpect(result -> {
                    JsonNode json = readBody(result);
                    assertEquals(10009, json.get("code").asInt());
                });
    }

    @Test
    void notGrantedRejectedWith403() throws Exception {
        long ts = System.currentTimeMillis();
        String nonce = "n6";
        when(appRepository.isGranted(APP_ID, PATH, "GET")).thenReturn(false);
        mvc.perform(get(PATH)
                        .header(ApiHeaders.APP_ID, APP_ID)
                        .header(ApiHeaders.TIMESTAMP, ts)
                        .header(ApiHeaders.NONCE, nonce)
                        .header(ApiHeaders.SIGN, sign(ts, nonce, "")))
                .andExpect(status().isForbidden())
                .andExpect(result -> {
                    JsonNode json = readBody(result);
                    assertEquals(10008, json.get("code").asInt());
                });
    }

    @Test
    void validRequestPassesAndSetsAttributes() throws Exception {
        long ts = System.currentTimeMillis();
        String nonce = "n7";
        MvcResult result = mvc.perform(get(PATH)
                        .header(ApiHeaders.APP_ID, APP_ID)
                        .header(ApiHeaders.TIMESTAMP, ts)
                        .header(ApiHeaders.NONCE, nonce)
                        .header(ApiHeaders.SIGN, sign(ts, nonce, "")))
                .andExpect(status().isOk())
                .andExpect(result2 -> {
                    JsonNode json = readBody(result2);
                    assertEquals(0, json.get("code").asInt());
                })
                .andReturn();

        Object userId = result.getRequest().getAttribute(RequestAttrs.USER_ID);
        Object openAppId = result.getRequest().getAttribute(RequestAttrs.OPEN_APP_ID);
        assertNotNull(userId);
        assertEquals("42", userId);
        assertEquals(APP_ID, openAppId);
        assertTrue(result.getRequest().getAttributeNames().hasMoreElements());
    }

    private JsonNode readBody(MvcResult result) throws Exception {
        return new ObjectMapper().readTree(result.getResponse().getContentAsString());
    }

    @RestController
    static class StubController {
        @GetMapping("/api/open/weather")
        public Result<String> weather() {
            return Result.ok("sunny");
        }

        @GetMapping("/api/open/health")
        public Result<String> health() {
            return Result.ok("UP");
        }
    }
}
