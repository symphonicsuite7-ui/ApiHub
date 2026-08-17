package com.apihub.admin.controller;

import com.apihub.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端接口骨架。后续实现：接口 CRUD、应用管理、开通授权、调用日志与统计。
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        return Result.ok(Map.of("service", "api-admin", "status", "UP"));
    }
}
