package com.apihub.invoke.advice;

import com.apihub.common.exception.BizException;
import com.apihub.common.result.ErrorCode;
import com.apihub.common.result.Result;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException ex) {
        return Result.<Void>fail(ex.getCode(), ex.getMessage()).traceId(MDC.get("traceId"));
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception ex) {
        return Result.<Void>fail(ErrorCode.INTERNAL_ERROR.getCode(), ex.getMessage()).traceId(MDC.get("traceId"));
    }
}
