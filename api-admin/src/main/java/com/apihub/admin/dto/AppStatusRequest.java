package com.apihub.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新应用状态请求。
 */
@Data
public class AppStatusRequest {

    /** 1 启用 0 禁用 */
    @NotNull(message = "status 不能为空")
    @Min(value = 0, message = "status 只能为 0（禁用）或 1（启用）")
    @Max(value = 1, message = "status 只能为 0（禁用）或 1（启用）")
    private Integer status;
}
