package com.accenture.meetspace.bootstrap.application.query;

public class InvalidQueryException extends RuntimeException {

    private final String field;
    private final String reason;

    public InvalidQueryException(String field, String reason) {
        super(field + ": " + reason);
        this.field = field;
        this.reason = reason;
    }

    public String field() {
        return field;
    }

    public String reason() {
        return reason;
    }
}
