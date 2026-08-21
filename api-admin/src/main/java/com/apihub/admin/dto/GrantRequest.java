package com.apihub.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 为应用开通接口请求。
 */
@Data
public class GrantRequest {

    /** 要开通的接口 id 列表 */
    private List<Long> interfaceIds;
}
