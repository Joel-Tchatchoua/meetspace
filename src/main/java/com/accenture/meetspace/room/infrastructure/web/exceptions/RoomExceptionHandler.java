package com.accenture.meetspace.room.infrastructure.web.exceptions;

import com.accenture.meetspace.shared.infrastructure.web.exceptions.ProblemDetailsExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;

@RestControllerAdvice(basePackages = "com.accenture.meetspace.room")
public class RoomExceptionHandler extends ProblemDetailsExceptionHandler {

    public RoomExceptionHandler(Clock clock) {
        super(clock);
    }

    @Override
    protected String service() {
        return "room";
    }
}
