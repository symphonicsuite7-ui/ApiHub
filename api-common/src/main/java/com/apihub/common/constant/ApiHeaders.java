package com.apihub.common.constant;

public final class ApiHeaders {

    public static final String TRACE_ID = "X-Trace-Id";
    public static final String AUTHORIZATION = "Authorization";
    public static final String APP_ID = "X-App-Id";
    public static final String TIMESTAMP = "X-Timestamp";
    public static final String NONCE = "X-Nonce";
    public static final String SIGN = "X-Sign";
    /** 网关解析 JWT 后透传给下游的用户身份头 */
    public static final String USER_ID = "X-User-Id";
    public static final String USER_NAME = "X-User-Name";
    public static final String USER_ROLES = "X-User-Roles";

    private ApiHeaders() {
    }
}
