package com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.requests;

import com.accenture.meetspace.user.auth.application.usecases.command.logout.LogoutCommand;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(@NotBlank String refreshToken) {

    public LogoutCommand toCommand() {
        return new LogoutCommand(refreshToken);
    }

    @Override
    public String toString() {
        return "LogoutRequest[]";
    }
}
