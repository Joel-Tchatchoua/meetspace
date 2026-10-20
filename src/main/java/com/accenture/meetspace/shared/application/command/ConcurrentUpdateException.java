package com.accenture.meetspace.shared.application.command;

public class ConcurrentUpdateException extends TransactionalException {

    public ConcurrentUpdateException(Command<?> command, Throwable cause) {
        super("Concurrent update while handling " + command.getClass().getSimpleName(), cause);
    }
}
