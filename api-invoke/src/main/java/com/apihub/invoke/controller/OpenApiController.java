package com.apihub.invoke.controller;

import com.apihub.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 开放业务接口骨架。经网关 /api/open/** 转发，需 App 签名鉴权。
 */
@RestController
@RequestMapping("/open")
public class OpenApiController {

    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        return Result.ok(Map.of("service", "api-invoke", "status", "UP"));
    }

    @GetMapping("/weather")
    public Result<Map<String, Object>> weather(@RequestParam(defaultValue = "北京") String city) {
        return Result.ok(Map.of(
                "city", city,
                "weather", "晴",
                "temperature", "26℃",
                "tip", "骨架演示数据，后续可替换为真实调用"
        ));
    }
}
