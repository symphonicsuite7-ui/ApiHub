package com.apihub.common.exception;

import com.apihub.common.result.ErrorCode;

/** 业务异常，由全局异常处理器转成统一返回体。 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
    }

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
