package com.accenture.meetspace.shared.application.command;

import java.util.Objects;

public record CommandHandlerRegistration<C extends Command<R>, R>(Class<C> commandType,
                                                                  CommandHandler<C, R> handler) {

    public CommandHandlerRegistration {
        Objects.requireNonNull(commandType, "commandType");
        Objects.requireNonNull(handler, "handler");
    }

    public R handle(Command<?> command) {
        return handler.handle(commandType.cast(command));
    }
}
