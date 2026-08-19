package com.apihub.invoke.service;

import com.apihub.invoke.client.AmapWeatherClient;
import com.apihub.invoke.config.WeatherProperties;
import com.apihub.invoke.dto.WeatherVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 天气业务层：选择数据源，将第三方结果转换为 ApiHub 统一模型。
 */
@Service
public class WeatherService {

    private final WeatherProperties properties;
    private final AmapWeatherClient amapWeatherClient;

    public WeatherService(WeatherProperties properties, AmapWeatherClient amapWeatherClient) {
        this.properties = properties;
        this.amapWeatherClient = amapWeatherClient;
    }

    public WeatherVO query(String city) {
        String normalizedCity = StringUtils.hasText(city) ? city.trim() : "北京";
        if (useAmap()) {
            return queryFromAmap(normalizedCity);
        }
        return mockWeather(normalizedCity);
    }

    private boolean useAmap() {
        return "amap".equalsIgnoreCase(properties.getProvider())
                && StringUtils.hasText(properties.getAmap().getKey());
    }

    private WeatherVO queryFromAmap(String city) {
        String adcode = amapWeatherClient.resolveAdcode(city);
        Map<String, Object> live = amapWeatherClient.fetchLiveWeather(adcode);

        WeatherVO vo = new WeatherVO();
        vo.setCity(String.valueOf(live.getOrDefault("city", city)));
        vo.setAdcode(String.valueOf(live.getOrDefault("adcode", adcode)));
        vo.setWeather(String.valueOf(live.get("weather")));
        vo.setTemperature(String.valueOf(live.get("temperature")) + "℃");
        vo.setHumidity(String.valueOf(live.get("humidity")));
        vo.setWindDirection(String.valueOf(live.get("winddirection")));
        vo.setWindPower(String.valueOf(live.get("windpower")) + "级");
        vo.setReportTime(String.valueOf(live.get("reporttime")));
        vo.setSource("amap");
        return vo;
    }

    /** 未配置 Key 时的演示数据，便于本地开发 */
    private WeatherVO mockWeather(String city) {
        WeatherVO vo = new WeatherVO();
        vo.setCity(city);
        vo.setWeather("晴");
        vo.setTemperature("26℃");
        vo.setHumidity("45");
        vo.setWindDirection("东南");
        vo.setWindPower("3级");
        vo.setReportTime("演示数据");
        vo.setSource("mock");
        return vo;
    }
}
