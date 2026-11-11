package com.accenture.meetspace.user.auth.application.usecases.command.logout;

import com.accenture.meetspace.bootstrap.application.command.Command;

import static com.accenture.meetspace.bootstrap.application.command.CommandPreconditions.requireNotBlank;

public record LogoutCommand(String refreshToken) implements Command<LogoutResponse> {

    public LogoutCommand {
        requireNotBlank(refreshToken, "refreshToken");
    }

    @Override
    public String toString() {
        return "LogoutCommand[]";
    }
}
