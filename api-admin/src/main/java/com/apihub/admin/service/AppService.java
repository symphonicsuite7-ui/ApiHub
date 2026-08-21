package com.apihub.admin.service;

import com.apihub.admin.dto.AppCreateRequest;
import com.apihub.admin.dto.AppVO;

import java.util.List;

/**
 * 开放应用管理：创建、列表、启禁用、开通/取消开通接口。
 */
public interface AppService {

    AppVO create(AppCreateRequest request, Long operatorId);

    List<AppVO> list(Long operatorId);

    AppVO detail(Long id, Long operatorId);

    void updateStatus(Long id, Integer status, Long operatorId);

    void grant(String appId, List<Long> interfaceIds, Long operatorId);

    void revoke(String appId, Long interfaceId, Long operatorId);
}
