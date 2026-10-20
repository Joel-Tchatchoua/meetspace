package com.accenture.meetspace.shared.application.command;

public class TransactionalException extends RuntimeException {

    public TransactionalException(Command<?> command, Throwable cause) {
        this("Transaction failed while handling " + command.getClass().getSimpleName(), cause);
    }

    protected TransactionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
