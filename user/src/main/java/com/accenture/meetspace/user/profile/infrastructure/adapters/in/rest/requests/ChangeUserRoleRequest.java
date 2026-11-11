package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.requests;

import com.accenture.meetspace.user.profile.application.usecases.command.changerole.ChangeUserRoleCommand;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChangeUserRoleRequest(@NotNull RoleEnum role) {

    public ChangeUserRoleCommand toCommand(UUID userId) {
        return new ChangeUserRoleCommand(userId, role);
    }
}
