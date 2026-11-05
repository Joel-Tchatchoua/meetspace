package com.accenture.meetspace.bootstrap.infrastructure.web.exceptions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;

@RestControllerAdvice
@Order()
public class GlobalExceptionHandler extends ProblemDetailsExceptionHandler {

    private final String service;

    public GlobalExceptionHandler(Clock clock, @Value("${spring.application.name}") String service) {
        super(clock);
        this.service = service;
    }

    @Override
    protected String service() {
        return service;
    }
}
