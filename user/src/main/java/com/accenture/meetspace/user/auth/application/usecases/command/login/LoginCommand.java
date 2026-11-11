package com.accenture.meetspace.user.auth.application.usecases.command.login;

import com.accenture.meetspace.bootstrap.application.command.Command;

import static com.accenture.meetspace.bootstrap.application.command.CommandPreconditions.requireNotBlank;

public record LoginCommand(String username, String password) implements Command<LoginResponse> {

    public LoginCommand {
        requireNotBlank(username, "username");
        requireNotBlank(password, "password");
    }

    @Override
    public String toString() {
        return "LoginCommand[username=" + username + "]";
    }
}
