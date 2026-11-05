package com.accenture.meetspace.bootstrap.infrastructure.config;

import com.accenture.meetspace.bootstrap.infrastructure.web.pagination.PaginationArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class PaginationConfiguration implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new PaginationArgumentResolver());
    }
}
