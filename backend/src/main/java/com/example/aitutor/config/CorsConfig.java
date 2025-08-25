package com.example.aitutor.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    
    config.setAllowedOriginPatterns(List.of(
      "http://localhost:5173", // Vite 本地開發用
      "https://aitutor-frontend-production.up.railway.app" // 前端部署網址
    ));
    
    config.addAllowedMethod("*");
    config.addAllowedHeader("*");
    config.setAllowCredentials(true); // 如果你使用 cookie/session 驗證要開啟

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    
    return new CorsFilter(source);
  }
}
