package com.apihub.admin.entity;

import com.apihub.admin.Enum.HttpMethodEnum;
import com.apihub.admin.Enum.StatusEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口资产实体，对应表 api_interface。
 */
@Data
@TableName("api_interface")
public class InterfaceEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String path;

    private HttpMethodEnum method;

    private String description;

    private String version;

    private String category;

    /** 0 下线 1 上线 */
    private StatusEnum status;

    private LocalDateTime createTime;
}
