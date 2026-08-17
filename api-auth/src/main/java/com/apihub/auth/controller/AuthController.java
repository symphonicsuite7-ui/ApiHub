package com.apihub.auth.controller;

import com.apihub.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证接口骨架。后续实现：register / login / logout / me
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        return Result.ok(Map.of("service", "api-auth", "status", "UP"));
    }
}
