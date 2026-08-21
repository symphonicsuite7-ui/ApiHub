package com.apihub.admin.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创建应用成功响应。AppSecret 仅在创建时返回一次，请调用方妥善保存。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppCreatedVO extends AppVO {

    private String appSecret;
}
