package com.apihub.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 骨架阶段排除数据源自动配置，便于无 MySQL 时先启动验证。
 * 实现管理持久化时去掉 exclude 即可。
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
