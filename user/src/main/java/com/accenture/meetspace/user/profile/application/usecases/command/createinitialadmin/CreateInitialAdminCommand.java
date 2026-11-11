package com.accenture.meetspace.user.profile.application.usecases.command.createinitialadmin;

import com.accenture.meetspace.bootstrap.application.command.Command;

import static com.accenture.meetspace.bootstrap.application.command.CommandPreconditions.requireNotBlank;

public record CreateInitialAdminCommand(String username) implements Command<CreateInitialAdminResponse> {

    public CreateInitialAdminCommand {
        requireNotBlank(username, "username");
    }
}
