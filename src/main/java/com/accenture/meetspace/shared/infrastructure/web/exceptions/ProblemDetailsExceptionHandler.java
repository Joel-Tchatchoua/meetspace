package com.accenture.meetspace.shared.infrastructure.web.exceptions;

import com.accenture.meetspace.shared.application.command.ConcurrentUpdateException;
import com.accenture.meetspace.shared.application.command.InvalidCommandException;
import com.accenture.meetspace.shared.application.command.TransactionalException;
import com.accenture.meetspace.shared.domain.BusinessRuleViolationException;
import com.accenture.meetspace.shared.domain.DomainValidationException;
import com.accenture.meetspace.shared.domain.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Clock;
import java.util.List;

public abstract class ProblemDetailsExceptionHandler extends ResponseEntityExceptionHandler {

    private final Clock clock;

    protected ProblemDetailsExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    protected abstract String service();

    @ExceptionHandler(InvalidCommandException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCommand(InvalidCommandException exception,
                                                              HttpServletRequest request) {
        var problem = invalidRequest(request.getRequestURI());
        problem.setProperty("errors", List.of(new InvalidField(exception.field(), exception.reason())));
        return respond(problem);
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ProblemDetail> handleDomainValidation(DomainValidationException exception,
                                                                HttpServletRequest request) {
        return respond(problem(HttpStatus.BAD_REQUEST, "Invalid request", exception.code(),
                exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(NotFoundException exception, HttpServletRequest request) {
        return respond(problem(HttpStatus.NOT_FOUND, "Resource not found", exception.code(),
                exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRuleViolation(BusinessRuleViolationException exception,
                                                                     HttpServletRequest request) {
        return respond(problem(HttpStatus.CONFLICT, "Business rule violated", exception.code(),
                exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ConcurrentUpdateException.class)
    public ResponseEntity<ProblemDetail> handleConcurrentUpdate(HttpServletRequest request) {
        return respond(problem(HttpStatus.CONFLICT, "Concurrent update", "CONCURRENT_UPDATE",
                "The resource was modified by another request, reload it and retry", request.getRequestURI()));
    }

    @ExceptionHandler(TransactionalException.class)
    public ResponseEntity<ProblemDetail> handleTransactional(TransactionalException exception,
                                                             HttpServletRequest request) {
        logger.error("Technical failure on " + request.getRequestURI(), exception);
        return respond(problem(HttpStatus.INTERNAL_SERVER_ERROR, "Technical error", "TECHNICAL_ERROR",
                "The request could not be completed", request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception, HttpServletRequest request) {
        logger.error("Unexpected failure on " + request.getRequestURI(), exception);
        return respond(problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", "UNEXPECTED_ERROR",
                "The request could not be completed", request.getRequestURI()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        var problem = invalidRequest(path(request));
        problem.setProperty("errors", exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new InvalidField(error.getField(), error.getDefaultMessage()))
                .toList());
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        var problem = problem(HttpStatus.BAD_REQUEST, "Malformed request", "MALFORMED_REQUEST",
                "The request body could not be read", path(request));
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        var response = super.handleExceptionInternal(exception, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem) {
            stamp(problem, path(request));
        }
        return response;
    }

    protected ProblemDetail problem(HttpStatus status, String title, String code, String detail, String path) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        stamp(problem, path);
        return problem;
    }

    protected static ResponseEntity<ProblemDetail> respond(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    private ProblemDetail invalidRequest(String path) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", "INVALID_REQUEST",
                "The request contains invalid fields", path);
    }

    private void stamp(ProblemDetail problem, String path) {
        if (problem.getInstance() == null) {
            problem.setInstance(URI.create(path));
        }
        problem.setProperty("service", service());
        problem.setProperty("timestamp", clock.instant());
    }

    private static String path(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }
}
