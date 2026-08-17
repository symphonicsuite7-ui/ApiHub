package com.apihub.auth.controller;

import com.apihub.auth.dto.LoginRequest;
import com.apihub.auth.dto.LoginVO;
import com.apihub.auth.dto.RegisterRequest;
import com.apihub.auth.service.AuthService;
import com.apihub.common.constant.ApiHeaders;
import com.apihub.common.exception.BizException;
import com.apihub.common.result.ErrorCode;
import com.apihub.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JdbcTemplate jdbcTemplate;

    public AuthController(AuthService authService, JdbcTemplate jdbcTemplate) {
        this.authService = authService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return Result.ok(Map.of(
                "service", "api-auth",
                "status", "UP",
                "db", one != null && one == 1 ? "UP" : "DOWN"
        ));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @GetMapping("/me")
    public Result<LoginVO> me(@RequestHeader(value = ApiHeaders.AUTHORIZATION, required = false) String authorization) {
        String token = extractToken(authorization);
        return Result.ok(authService.currentUser(token));
    }

    private String extractToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return authorization.substring(7).trim();
    }
}
