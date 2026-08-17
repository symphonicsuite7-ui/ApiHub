package com.apihub.gateway.controller;

import com.apihub.common.result.Result;
import com.apihub.gateway.config.RouteProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final RouteProperties routeProperties;

    public HealthController(RouteProperties routeProperties) {
        this.routeProperties = routeProperties;
    }

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("service", "api-gateway");
        data.put("status", "UP");
        data.put("routes", Map.of(
                "auth", routeProperties.getAuth(),
                "admin", routeProperties.getAdmin(),
                "invoke", routeProperties.getInvoke()
        ));
        return Result.ok(data);
    }
}
