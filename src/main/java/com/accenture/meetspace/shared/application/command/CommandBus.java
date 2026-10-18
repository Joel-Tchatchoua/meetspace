package com.accenture.meetspace.shared.application.command;

public interface CommandBus {

    void dispatch(Command command);
}
