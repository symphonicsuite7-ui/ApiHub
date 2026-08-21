package com.apihub.admin.controller;

import com.apihub.admin.dto.AppCreateRequest;
import com.apihub.admin.dto.AppCreatedVO;
import com.apihub.admin.dto.AppDetailVO;
import com.apihub.admin.dto.AppStatusRequest;
import com.apihub.admin.dto.AppVO;
import com.apihub.admin.dto.GrantRequest;
import com.apihub.admin.service.AppService;
import com.apihub.common.constant.ApiHeaders;
import com.apihub.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 开放应用管理接口。
 * <p>
 * 经网关访问时，X-User-Id / X-User-Roles 由网关从 JWT 解析注入；
 * 本地直连调试时 X-User-Id 可缺失（回退请求体 userId），X-User-Roles 缺失视为普通用户。
 */
@Tag(name = "应用管理", description = "开放应用与应用-接口开通关系管理")
@RestController
@RequestMapping("/admin/apps")
public class AppController {

    private final AppService appService;

    public AppController(AppService appService) {
        this.appService = appService;
    }

    @Operation(summary = "应用列表", description = "普通用户仅返回本人应用，管理员返回全部")
    @GetMapping
    public Result<List<AppVO>> list(@RequestHeader(value = ApiHeaders.USER_ID, required = false) String userId,
                                    @RequestHeader(value = ApiHeaders.USER_ROLES, required = false) String roles) {
        return Result.ok(appService.list(parseUserId(userId), roles));
    }

    @Operation(summary = "创建应用", description = "生成 AppId/AppSecret；AppSecret 仅在本次响应返回一次")
    @PostMapping
    public Result<AppCreatedVO> create(@Valid @RequestBody AppCreateRequest request,
                                       @RequestHeader(value = ApiHeaders.USER_ID, required = false) String userId,
                                       @RequestHeader(value = ApiHeaders.USER_ROLES, required = false) String roles) {
        return Result.ok(appService.create(request, parseUserId(userId), roles));
    }

    @Operation(summary = "应用详情", description = "包含已开通的接口列表")
    @GetMapping("/{id}")
    public Result<AppDetailVO> detail(@PathVariable Long id,
                                      @RequestHeader(value = ApiHeaders.USER_ID, required = false) String userId,
                                      @RequestHeader(value = ApiHeaders.USER_ROLES, required = false) String roles) {
        return Result.ok(appService.detail(id, parseUserId(userId), roles));
    }

    @Operation(summary = "启用/禁用应用")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Valid @RequestBody AppStatusRequest request,
                                     @RequestHeader(value = ApiHeaders.USER_ID, required = false) String userId,
                                     @RequestHeader(value = ApiHeaders.USER_ROLES, required = false) String roles) {
        appService.updateStatus(id, request.getStatus(), parseUserId(userId), roles);
        return Result.ok();
    }

    @Operation(summary = "为应用开通接口", description = "接口须已上线；重复开通报错")
    @PostMapping("/{appId}/grants")
    public Result<Void> grant(@PathVariable String appId,
                              @Valid @RequestBody GrantRequest request,
                              @RequestHeader(value = ApiHeaders.USER_ID, required = false) String userId,
                              @RequestHeader(value = ApiHeaders.USER_ROLES, required = false) String roles) {
        appService.grant(appId, request.getInterfaceIds(), parseUserId(userId), roles);
        return Result.ok();
    }

    @Operation(summary = "取消开通接口")
    @DeleteMapping("/{appId}/grants/{interfaceId}")
    public Result<Void> revoke(@PathVariable String appId,
                               @PathVariable Long interfaceId,
                               @RequestHeader(value = ApiHeaders.USER_ID, required = false) String userId,
                               @RequestHeader(value = ApiHeaders.USER_ROLES, required = false) String roles) {
        appService.revoke(appId, interfaceId, parseUserId(userId), roles);
        return Result.ok();
    }

    private Long parseUserId(String headerValue) {
        return StringUtils.hasText(headerValue) ? Long.parseLong(headerValue.trim()) : null;
    }
}
