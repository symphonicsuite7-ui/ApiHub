package com.apihub.common.constant;

/**
 * 网关过滤器之间传递用户身份的 Request Attribute 名。
 * JwtAuthFilter 写入，GatewayProxyFilter 读取并透传为请求头。
 */
public final class RequestAttrs {

    public static final String USER_ID = "apihub.userId";
    public static final String USER_NAME = "apihub.username";
    public static final String USER_ROLES = "apihub.roles";
    /** 开放调用通过签名认证的应用标识（OpenApiAuthFilter 写入） */
    public static final String OPEN_APP_ID = "apihub.openAppId";

    private RequestAttrs() {
    }
}