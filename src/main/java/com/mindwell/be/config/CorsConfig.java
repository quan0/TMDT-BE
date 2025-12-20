package com.mindwell.be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 1. Cho phép các nguồn (Origins) cụ thể gọi vào
        // Thay bằng địa chỉ Frontend của bạn, ví dụ: "http://localhost:3000"
        config.setAllowedOriginPatterns(Collections.singletonList("*"));

        // 2. Cho phép các Header quan trọng (để gửi JWT Token)
        config.setAllowedHeaders(Arrays.asList(
                "Origin", "Content-Type", "Accept", "Authorization",
                "Cache-Control", "X-Requested-With"
        ));

        // 3. Cho phép các phương thức HTTP
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 4. Cho phép gửi Cookie hoặc Authentication Header (Cần thiết cho JWT)
        config.setAllowCredentials(true);

        // 5. Áp dụng cấu hình cho tất cả các endpoint (bao gồm cả api/v1 và swagger)
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}