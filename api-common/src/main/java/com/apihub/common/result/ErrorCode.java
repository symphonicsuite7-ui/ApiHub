package com.apihub.common.result;

public enum ErrorCode {
    SUCCESS(0, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证或 Token 无效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "系统异常"),

    USER_EXISTS(10001, "用户名已存在"),
    USER_PASSWORD_ERROR(10002, "用户名或密码错误"),
    APP_INVALID(10003, "应用不存在或已禁用"),
    SIGN_INVALID(10004, "签名校验失败"),
    TIMESTAMP_INVALID(10005, "时间戳无效或请求过期"),
    NONCE_REPLAY(10006, "重复请求"),
    INTERFACE_OFFLINE(10007, "接口未上线"),
    INTERFACE_NOT_GRANTED(10008, "未开通该接口"),
    QUOTA_EXCEEDED(10009, "调用配额已用尽");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
