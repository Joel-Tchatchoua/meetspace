package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.requests;

import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusCommand;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChangeUserStatusRequest(@NotNull Boolean active) {

    public ChangeUserStatusCommand toCommand(UUID actorId, UUID userId) {
        return new ChangeUserStatusCommand(actorId, userId, active);
    }
}
