package com.accenture.meetspace.shared.application.command;

public class InvalidCommandException extends RuntimeException {

    private final String field;
    private final String reason;

    public InvalidCommandException(String field, String reason) {
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
