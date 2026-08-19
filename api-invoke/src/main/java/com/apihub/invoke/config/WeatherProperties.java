package com.apihub.invoke.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 天气能力配置：第三方 Key 仅保存在 invoke 服务，不暴露给开放调用方。
 */
@ConfigurationProperties(prefix = "apihub.weather")
public class WeatherProperties {

    /** 数据源：amap=高德，mock=本地演示 */
    private String provider = "mock";

    private final Amap amap = new Amap();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Amap getAmap() {
        return amap;
    }

    public static class Amap {
        /** 高德 Web 服务 Key，建议通过环境变量 AMAP_KEY 注入 */
        private String key = "";
        private String baseUrl = "https://restapi.amap.com";

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
