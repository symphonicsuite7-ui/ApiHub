package com.apihub.common.exception;

import com.apihub.common.result.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BizExceptionTest {

    @Test
    void constructorWithErrorCodeShouldSetMessageAndCode() {
        BizException exception = new BizException(ErrorCode.USER_EXISTS);

        assertEquals(ErrorCode.USER_EXISTS.getCode(), exception.getCode());
        assertEquals(ErrorCode.USER_EXISTS.getMsg(), exception.getMessage());
    }

    @Test
    void constructorWithCustomCodeAndMessageShouldSetFields() {
        BizException exception = new BizException(10010, "custom failure");

        assertEquals(10010, exception.getCode());
        assertEquals("custom failure", exception.getMessage());
    }
}
