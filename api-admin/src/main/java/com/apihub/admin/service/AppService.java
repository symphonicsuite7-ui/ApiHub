package com.apihub.admin.service;

import com.apihub.admin.dto.AppCreateRequest;
import com.apihub.admin.dto.AppCreatedVO;
import com.apihub.admin.dto.AppDetailVO;
import com.apihub.admin.dto.AppVO;

import java.util.List;

/**
 * 开放应用管理：创建、列表、启禁用、开通/取消开通接口。
 */
public interface AppService {

    AppCreatedVO create(AppCreateRequest request, Long operatorId, String rolesHeader);

    List<AppVO> list(Long operatorId, String rolesHeader);

    AppDetailVO detail(Long id, Long operatorId, String rolesHeader);

    void updateStatus(Long id, Integer status, Long operatorId, String rolesHeader);

    void grant(String appId, List<Long> interfaceIds, Long operatorId, String rolesHeader);

    void revoke(String appId, Long interfaceId, Long operatorId, String rolesHeader);
}
