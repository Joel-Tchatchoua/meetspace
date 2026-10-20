package com.accenture.meetspace.shared.application.command;

public interface CommandBus {

    <R> R dispatch(Command<R> command);
}
