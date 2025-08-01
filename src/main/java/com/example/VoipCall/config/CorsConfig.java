package com.example.VoipCall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*") // Allow all origins
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        // .allowCredentials(true) // Must be disabled when allowing all origins
                        .maxAge(3600);
            }
        };
    }
}