package com.apihub.admin.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 应用信息响应（v1 简单版：创建/详情时携带 AppSecret）。
 */
@Data
public class AppVO {

    private Long id;
    private String appId;
    private String appSecret;
    private String appName;
    private Long userId;
    private Integer status;
    private Integer qpsLimit;
    private Integer dailyQuota;
    private String createTime;

    /** 已开通的接口列表，仅详情接口返回 */
    private List<Map<String, Object>> grantedInterfaces;
}
