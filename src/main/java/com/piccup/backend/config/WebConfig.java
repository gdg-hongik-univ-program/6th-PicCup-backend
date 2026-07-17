package com.piccup.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000") // 프론트엔드 포트 맞춰서 수정 브라우저 기본차단 해제
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowCredentials(true) // 민감한 쿠키 허용
                .maxAge(3600);
        // 세션 로그인 구현은 프록시로 안됨
        // 프론트엔드 (Axios/Fetch): withCredentials: true 필요
    }
}

