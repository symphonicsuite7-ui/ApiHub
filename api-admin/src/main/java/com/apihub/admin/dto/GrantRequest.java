package com.apihub.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 为应用开通接口请求。
 */
@Data
public class GrantRequest {

    @NotEmpty(message = "interfaceIds 不能为空")
    private List<@NotNull(message = "接口 id 不能为空") Long> interfaceIds;
}
