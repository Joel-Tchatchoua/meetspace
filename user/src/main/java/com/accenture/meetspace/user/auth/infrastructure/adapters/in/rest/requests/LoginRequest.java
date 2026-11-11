package com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.requests;

import com.accenture.meetspace.user.auth.application.usecases.command.login.LoginCommand;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String username, @NotBlank String password) {

    public LoginCommand toCommand() {
        return new LoginCommand(username, password);
    }

    @Override
    public String toString() {
        return "LoginRequest[username=" + username + "]";
    }
}
