package com.apihub.gateway.open;

/**
 * 开放应用信息（由 api_app 表查询得到）。
 *
 * @param id         应用主键
 * @param appId      对外应用标识
 * @param appSecret  应用密钥（仅网关内部使用，用于验签）
 * @param userId     归属用户
 * @param status     1 启用 0 禁用
 * @param qpsLimit   QPS 上限
 * @param dailyQuota 每日调用配额
 */
public record AppInfo(long id, String appId, String appSecret, long userId, int status,
                      int qpsLimit, int dailyQuota) {
}
