package com.accenture.meetspace.shared.application.command;

@FunctionalInterface
public interface CommandHandler<C extends Command> {

    void handle(C command);
}
