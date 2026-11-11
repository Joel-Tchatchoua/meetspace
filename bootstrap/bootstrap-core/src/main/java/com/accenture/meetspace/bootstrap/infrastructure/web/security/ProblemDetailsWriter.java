package com.accenture.meetspace.bootstrap.infrastructure.web.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Clock;
import java.util.LinkedHashMap;

@Component
public class ProblemDetailsWriter {

    private final JsonMapper jsonMapper;
    private final Clock clock;
    private final String service;

    public ProblemDetailsWriter(JsonMapper jsonMapper, Clock clock,
                                @Value("${spring.application.name}") String service) {
        this.jsonMapper = jsonMapper;
        this.clock = clock;
        this.service = service;
    }

    public void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String title,
                      String code, String detail) throws IOException {
        var body = new LinkedHashMap<String, Object>();
        body.put("title", title);
        body.put("status", status.value());
        body.put("detail", detail);
        body.put("instance", request.getRequestURI());
        body.put("code", code);
        body.put("service", service);
        body.put("timestamp", clock.instant().toString());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getOutputStream().write(jsonMapper.writeValueAsBytes(body));
    }
}
