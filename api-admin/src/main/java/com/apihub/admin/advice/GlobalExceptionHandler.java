package com.apihub.admin.advice;

import com.apihub.common.exception.BizException;
import com.apihub.common.result.ErrorCode;
import com.apihub.common.result.Result;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * api-admin 全局异常处理：统一转换为 Result 返回体。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException ex) {
        return Result.<Void>fail(ex.getCode(), ex.getMessage()).traceId(MDC.get("traceId"));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValid(Exception ex) {
        String msg = "请求参数错误";
        if (ex instanceof MethodArgumentNotValidException manv && manv.getBindingResult().getFieldError() != null) {
            msg = manv.getBindingResult().getFieldError().getDefaultMessage();
        } else if (ex instanceof BindException be && be.getBindingResult().getFieldError() != null) {
            msg = be.getBindingResult().getFieldError().getDefaultMessage();
        }
        return Result.<Void>fail(ErrorCode.BAD_REQUEST.getCode(), msg).traceId(MDC.get("traceId"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleBody() {
        return Result.<Void>fail(ErrorCode.BAD_REQUEST).traceId(MDC.get("traceId"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleConflict(DataIntegrityViolationException ex) {
        return Result.<Void>fail(ErrorCode.BAD_REQUEST.getCode(), "数据冲突，请检查是否重复提交").traceId(MDC.get("traceId"));
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception ex) {
        return Result.<Void>fail(ErrorCode.INTERNAL_ERROR.getCode(), ex.getMessage()).traceId(MDC.get("traceId"));
    }
}
