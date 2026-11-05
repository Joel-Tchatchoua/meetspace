package com.accenture.meetspace.bootstrap.application.command;

@FunctionalInterface
public interface CommandHandler<C extends Command<R>, R> {

    R handle(C command);
}
