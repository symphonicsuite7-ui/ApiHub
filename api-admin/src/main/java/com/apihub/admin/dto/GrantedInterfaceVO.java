package com.apihub.admin.dto;

import lombok.Data;

/**
 * 应用已开通的接口信息。
 */
@Data
public class GrantedInterfaceVO {

    private Long id;
    private String name;
    private String path;
    private String method;
    private String description;
    private String version;
    private String category;
    private Integer status;
}
