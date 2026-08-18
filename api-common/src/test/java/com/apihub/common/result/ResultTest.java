package com.apihub.common.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultTest {

    @Test
    void okWithDataShouldPopulateSuccessResult() {
        String data = "ok";

        Result<String> result = Result.ok(data);

        assertEquals(0, result.getCode());
        assertEquals("success", result.getMsg());
        assertEquals(data, result.getData());
        assertNull(result.getTraceId());
    }

    @Test
    void okWithoutDataShouldCreateEmptySuccessResult() {
        Result<Void> result = Result.ok();

        assertEquals(0, result.getCode());
        assertEquals("success", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void failWithCodeAndMessageShouldPopulateErrorResult() {
        Result<String> result = Result.fail(400, "bad request");

        assertEquals(400, result.getCode());
        assertEquals("bad request", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void failWithErrorCodeShouldUseEnumValues() {
        Result<Object> result = Result.fail(ErrorCode.USER_EXISTS);

        assertEquals(ErrorCode.USER_EXISTS.getCode(), result.getCode());
        assertEquals(ErrorCode.USER_EXISTS.getMsg(), result.getMsg());
    }

    @Test
    void traceIdShouldBeSettable() {
        Result<Object> result = Result.ok().traceId("trace-123");

        assertEquals("trace-123", result.getTraceId());
    }
}
