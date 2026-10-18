package com.accenture.meetspace.shared.application.command;

public class InvalidCommandException extends RuntimeException {

    private final String field;

    public InvalidCommandException(String field, String message) {
        super(field + ": " + message);
        this.field = field;
    }

    public String field() {
        return field;
    }
}
