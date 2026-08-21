package com.apihub.admin.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 应用详情响应：基础信息 + 已开通接口列表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppDetailVO extends AppVO {

    private List<GrantedInterfaceVO> grantedInterfaces;
}
