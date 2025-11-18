package com.example.kolla.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final DateTimeConverter dateTimeConverter;

    public WebConfig(DateTimeConverter dateTimeConverter) {
        this.dateTimeConverter = dateTimeConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(dateTimeConverter);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Cấu hình chung cho tất cả các endpoints
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:4200",  // Angular dev server
                    "http://localhost:3000",   // React dev server
                    "http://localhost:8080",   // Vue dev server
                    "https://localhost:8080",   // Vue dev server
                    "http://localhost:8888",   // Local dev
                    "https://kolla.click",     // Production
                    "https://signal.kolla.click", 
                    "https://meeting.kolla.click",
                    "https://meet.kolla.click",
                    "https://kma.kolla.click",
                    "https://36.50.54.109",
                    "https://36.50.54.109:8081"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", 
                              "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers")
                .allowCredentials(true)
                .maxAge(3600); // Cache CORS config trong 1 giờ
    }
}