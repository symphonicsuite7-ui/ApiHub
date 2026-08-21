package com.apihub.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建开放应用请求。
 */
@Data
public class AppCreateRequest {

    @NotBlank(message = "应用名称不能为空")
    @Size(max = 128, message = "应用名称最长 128 字符")
    private String appName;

    /** QPS 限制，缺省 10 */
    @Min(value = 1, message = "QPS 限制最小为 1")
    @Max(value = 100000, message = "QPS 限制最大为 100000")
    private Integer qpsLimit;

    /** 每日配额，缺省 1000 */
    @Min(value = 1, message = "每日配额最小为 1")
    @Max(value = 100000000, message = "每日配额数值过大")
    private Integer dailyQuota;

    /** 仅本地直连调试时使用；经网关调用时以 X-User-Id 为准 */
    private Long userId;
}
