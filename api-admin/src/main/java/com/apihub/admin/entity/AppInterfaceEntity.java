package com.apihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用开通接口关系实体，对应表 api_app_interface。
 */
@Data
@TableName("api_app_interface")
public class AppInterfaceEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String appId;

    private Long interfaceId;

    /** 剩余调用次数，空表示不限 */
    private Integer remainCount;

    private LocalDateTime createTime;
}
