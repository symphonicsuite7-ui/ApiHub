package com.apihub.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用信息响应（不携带 AppSecret，防泄露）。
 */
@Data
public class AppVO {

    private Long id;
    private String appId;
    private String appName;
    private Long userId;
    private Integer status;
    private Integer qpsLimit;
    private Integer dailyQuota;
    private LocalDateTime createTime;
}
