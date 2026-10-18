package com.accenture.meetspace.shared.application.command;

import java.util.Objects;

public record CommandHandlerRegistration<C extends Command>(Class<C> commandType, CommandHandler<C> handler) {

    public CommandHandlerRegistration {
        Objects.requireNonNull(commandType, "commandType");
        Objects.requireNonNull(handler, "handler");
    }

    public void handle(Command command) {
        handler.handle(commandType.cast(command));
    }
}
