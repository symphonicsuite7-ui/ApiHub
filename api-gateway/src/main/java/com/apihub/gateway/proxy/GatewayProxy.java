package com.apihub.gateway.proxy;

/**
 * 反向代理占位：将 /api/auth/** → auth、/api/admin/** → admin、/api/open/** → invoke。
 * 下一阶段在此实现 RestTemplate/WebClient 转发，并挂载鉴权与限流。
 */
public interface GatewayProxy {
}
