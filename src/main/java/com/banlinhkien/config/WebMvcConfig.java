package com.banlinhkien.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    public static final String UPLOAD_DIR = "uploads/products/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ensure upload directory exists
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Serve uploaded files from external folder first, fallback to classpath static assets
        registry.addResourceHandler("/img/products/**")
                .addResourceLocations("file:" + UPLOAD_DIR, "classpath:/static/img/products/");
    }
}
