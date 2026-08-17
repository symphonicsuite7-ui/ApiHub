package com.apihub.auth.config;

import com.apihub.common.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtUtil jwtUtil(
            @Value("${apihub.jwt.secret}") String secret,
            @Value("${apihub.jwt.expire-seconds:7200}") long expireSeconds
    ) {
        return new JwtUtil(secret, expireSeconds);
    }
}
