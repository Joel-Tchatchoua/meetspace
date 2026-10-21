package com.accenture.meetspace.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class ApiVersioningConfiguration implements WebMvcConfigurer {

    private static final int VERSION_SEGMENT = 1;

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer.usePathSegment(VERSION_SEGMENT,
                path -> path.pathWithinApplication().value().startsWith("/api/"));
    }
}
