package com.apihub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 开放应用实体，对应表 api_app。
 */
@Data
@TableName("api_app")
public class AppEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 对外应用标识，网关开放调用鉴权使用 */
    private String appId;

    /** 应用密钥，创建时生成，仅创建响应返回一次 */
    private String appSecret;

    private String appName;

    /** 归属用户 */
    private Long userId;

    /** 1 启用 0 禁用 */
    private Integer status;

    private Integer qpsLimit;

    private Integer dailyQuota;

    private LocalDateTime createTime;
}
