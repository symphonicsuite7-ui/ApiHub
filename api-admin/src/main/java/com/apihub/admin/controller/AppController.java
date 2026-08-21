package com.apihub.admin.controller;

import com.apihub.admin.dto.AppCreateRequest;
import com.apihub.admin.dto.AppVO;
import com.apihub.admin.dto.GrantRequest;
import com.apihub.admin.service.AppService;
import com.apihub.common.constant.ApiHeaders;
import com.apihub.common.result.Result;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 开放应用管理接口（v1 简单版）。
 * <p>
 * 经网关访问时，X-User-Id 由网关从 JWT 解析注入；本地直连调试时可缺失。
 */
@RestController
@RequestMapping("/admin/apps")
public class AppController {

    private final AppService appService;

    public AppController(AppService appService) {
        this.appService = appService;
    }

    @GetMapping
    public Result<List<AppVO>> list(@RequestHeader(value = ApiHeaders.USER_ID, required = false) String userId) {
        return Result.ok(appService.list(parseUserId(userId)));
    }

    @PostMapping
    public Result<AppVO> create(@RequestBody AppCreateRequest request,
                                @RequestHeader(value = ApiHeaders.USER_ID, required = false) String userId) {
        return Result.ok(appService.create(request, parseUserId(userId)));
    }

    @GetMapping("/{id}")
    public Result<AppVO> detail(@PathVariable Long id,
                                @RequestHeader(value = ApiHeaders.USER_ID, required = false) String userId) {
        return Result.ok(appService.detail(id, parseUserId(userId)));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status,
                                     @RequestHeader(value = ApiHeaders.USER_ID, required = false) String userId) {
        appService.updateStatus(id, status, parseUserId(userId));
        return Result.ok();
    }

    @PostMapping("/{appId}/grants")
    public Result<Void> grant(@PathVariable String appId,
                              @RequestBody GrantRequest request,
                              @RequestHeader(value = ApiHeaders.USER_ID, required = false) String userId) {
        appService.grant(appId, request == null ? null : request.getInterfaceIds(), parseUserId(userId));
        return Result.ok();
    }

    @DeleteMapping("/{appId}/grants/{interfaceId}")
    public Result<Void> revoke(@PathVariable String appId,
                               @PathVariable Long interfaceId,
                               @RequestHeader(value = ApiHeaders.USER_ID, required = false) String userId) {
        appService.revoke(appId, interfaceId, parseUserId(userId));
        return Result.ok();
    }

    private Long parseUserId(String headerValue) {
        return StringUtils.hasText(headerValue) ? Long.parseLong(headerValue.trim()) : null;
    }
}
