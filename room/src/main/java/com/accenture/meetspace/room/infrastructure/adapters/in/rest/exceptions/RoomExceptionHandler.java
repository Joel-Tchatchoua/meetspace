package com.accenture.meetspace.room.infrastructure.adapters.in.rest.exceptions;

import com.accenture.meetspace.bootstrap.infrastructure.web.exceptions.ProblemDetailsExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;

@RestControllerAdvice(basePackages = "com.accenture.meetspace.room")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RoomExceptionHandler extends ProblemDetailsExceptionHandler {

    public RoomExceptionHandler(Clock clock) {
        super(clock);
    }

    @Override
    protected String service() {
        return "room";
    }
}
