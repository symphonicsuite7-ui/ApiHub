package com.apihub.invoke.client;

import com.apihub.common.exception.BizException;
import com.apihub.common.result.ErrorCode;
import com.apihub.invoke.config.WeatherProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * 高德地图天气客户端：负责 HTTP 调用与原始 JSON 解析，不做 ApiHub 统一包装。
 */
@Component
public class AmapWeatherClient {

    private final RestTemplate restTemplate;
    private final WeatherProperties properties;

    public AmapWeatherClient(RestTemplate restTemplate, WeatherProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    /** 城市名或 adcode → adcode（高德 weather 接口要求 adcode） */
    public String resolveAdcode(String city) {
        if (!StringUtils.hasText(city)) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "city 不能为空");
        }
        if (city.matches("\\d{6}")) {
            return city;
        }
        String key = requireKey();
        String url = UriComponentsBuilder
                .fromHttpUrl(properties.getAmap().getBaseUrl() + "/v3/geocode/geo")
                .queryParam("address", city)
                .queryParam("key", key)
                .build()
                .toUriString();
        @SuppressWarnings("unchecked")
        Map<String, Object> body = restTemplate.getForObject(url, Map.class);
        assertAmapOk(body);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> geocodes = (List<Map<String, Object>>) body.get("geocodes");
        if (geocodes == null || geocodes.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "未找到城市: " + city);
        }
        return String.valueOf(geocodes.get(0).get("adcode"));
    }

    /** 查询实况天气，返回高德 lives 第一条 */
    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchLiveWeather(String adcode) {
        String key = requireKey();
        String url = UriComponentsBuilder
                .fromHttpUrl(properties.getAmap().getBaseUrl() + "/v3/weather/weatherInfo")
                .queryParam("city", adcode)
                .queryParam("extensions", "base")
                .queryParam("output", "JSON")
                .queryParam("key", key)
                .build()
                .toUriString();
        Map<String, Object> body = restTemplate.getForObject(url, Map.class);
        assertAmapOk(body);
        List<Map<String, Object>> lives = (List<Map<String, Object>>) body.get("lives");
        if (lives == null || lives.isEmpty()) {
            throw new BizException(ErrorCode.INTERNAL_ERROR.getCode(), "高德未返回天气数据");
        }
        return lives.get(0);
    }

    private String requireKey() {
        String key = properties.getAmap().getKey();
        if (!StringUtils.hasText(key)) {
            throw new BizException(ErrorCode.INTERNAL_ERROR.getCode(), "未配置高德 Key，请设置 apihub.weather.amap.key");
        }
        return key;
    }

    private void assertAmapOk(Map<String, Object> body) {
        if (body == null) {
            throw new BizException(ErrorCode.INTERNAL_ERROR.getCode(), "高德接口无响应");
        }
        String status = String.valueOf(body.get("status"));
        if (!"1".equals(status)) {
            String info = String.valueOf(body.get("info"));
            throw new BizException(ErrorCode.INTERNAL_ERROR.getCode(), "高德接口错误: " + info);
        }
    }
}
