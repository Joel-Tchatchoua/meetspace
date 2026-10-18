package com.accenture.meetspace.shared.application.command;

public class TransactionalException extends RuntimeException {

    public TransactionalException(Command command, Throwable cause) {
        super("Transaction failed while handling " + command.getClass().getSimpleName(), cause);
    }
}
