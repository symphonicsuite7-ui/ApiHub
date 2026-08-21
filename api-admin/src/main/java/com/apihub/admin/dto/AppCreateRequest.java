package com.apihub.admin.dto;

import lombok.Data;

/**
 * 创建开放应用请求（v1 简单版：仅校验必填）。
 */
@Data
public class AppCreateRequest {

    /** 应用名称，必填 */
    private String appName;

    /** QPS 限制，默认 10 */
    private Integer qpsLimit;

    /** 每日配额，默认 1000 */
    private Integer dailyQuota;

    /** 归属用户（仅本地直连调试时使用；经网关调用时以 X-User-Id 为准） */
    private Long userId;
}
