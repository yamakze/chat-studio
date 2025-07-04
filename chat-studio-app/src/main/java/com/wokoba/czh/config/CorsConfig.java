package com.wokoba.czh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true); // 允许携带 Cookie
        config.setAllowedOrigins(List.of("*")); // 允许前端访问
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE")); // 允许的 HTTP 方法
        config.setAllowedHeaders(List.of("*")); // 允许所有请求头

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
