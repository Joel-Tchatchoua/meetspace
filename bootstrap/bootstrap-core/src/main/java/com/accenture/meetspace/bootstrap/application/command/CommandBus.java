package com.accenture.meetspace.bootstrap.application.command;

public interface CommandBus {

    <R> R dispatch(Command<R> command);
}
