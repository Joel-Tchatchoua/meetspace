package com.accenture.meetspace.user.shared.infrastructure.adapters.in.rest.exceptions;

import com.accenture.meetspace.bootstrap.infrastructure.web.exceptions.ProblemDetailsExceptionHandler;
import com.accenture.meetspace.user.auth.domain.exceptions.AccountDisabledException;
import com.accenture.meetspace.user.auth.domain.exceptions.InvalidCredentialsException;
import com.accenture.meetspace.user.profile.domain.exceptions.ExternalServiceUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;

@RestControllerAdvice(basePackages = "com.accenture.meetspace.user")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserExceptionHandler extends ProblemDetailsExceptionHandler {

    public UserExceptionHandler(Clock clock) {
        super(clock);
    }

    @Override
    protected String service() {
        return "user";
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException exception,
                                                                  HttpServletRequest request) {
        return respond(problem(HttpStatus.UNAUTHORIZED, "Authentication failed", exception.code(),
                exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ProblemDetail> handleAccountDisabled(AccountDisabledException exception,
                                                               HttpServletRequest request) {
        return respond(problem(HttpStatus.FORBIDDEN, "Account disabled", exception.code(),
                exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleUnavailable(ExternalServiceUnavailableException exception,
                                                           HttpServletRequest request) {
        return respond(problem(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable", exception.code(),
                exception.getMessage(), request.getRequestURI()));
    }
}
