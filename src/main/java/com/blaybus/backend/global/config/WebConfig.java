package com.blaybus.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.worksheets-dir:uploads/worksheets}")
    private String worksheetsDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
    /**
     * 로컬에 저장된 파일을 URL로 접근 가능하게 해줌
     * 예) GET /files/worksheets/abc.pdf  →  (로컬) uploads/worksheets/abc.pdf
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path absolutePath = Paths.get(worksheetsDir).toAbsolutePath().normalize();

        registry.addResourceHandler("/files/worksheets/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}
