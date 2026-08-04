package com.piccup.backend.config;

import com.piccup.backend.interceptor.LoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .addPathPatterns("/api/**") // 검사할 기본 경로: /api로 시작하는 모든 요청
                .excludePathPatterns(       // 인터셉터 검사에서 제외할 예외 경로
                        "/api/users/signup",
                        "/api/users/login",
                        "/api/users/password/reset",
                        "/health",          // 서버 헬스체크용
                        "/swagger-ui/**"   // API 문서
                );
    }

    @Override
    public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:3000", "https://6th-piccup-frontend.vercel.app") // 프론트측 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); // 세션 쿠키를 주고받을 수 있게 허용
    }
}