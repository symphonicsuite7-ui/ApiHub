package com.apihub.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 骨架阶段排除数据源自动配置，便于无 MySQL 时先启动验证。
 * 实现登录持久化时去掉 exclude 即可。
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
