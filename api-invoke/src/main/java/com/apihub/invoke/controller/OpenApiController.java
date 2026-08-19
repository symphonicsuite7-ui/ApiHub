package com.apihub.invoke.controller;

import com.apihub.common.result.Result;
import com.apihub.invoke.dto.WeatherVO;
import com.apihub.invoke.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 开放业务接口。经网关 /api/open/** 转发，需 App 签名鉴权（网关侧实现）。
 */
@RestController
@RequestMapping("/open")
public class OpenApiController {

    private final WeatherService weatherService;

    public OpenApiController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        return Result.ok(Map.of("service", "api-invoke", "status", "UP"));
    }

    /**
     * ApiHub 统一天气接口：对外只暴露此路径，内部可切换高德/腾讯等实现。
     */
    @GetMapping("/weather")
    public Result<WeatherVO> weather(@RequestParam(defaultValue = "北京") String city) {
        return Result.ok(weatherService.query(city));
    }
}
