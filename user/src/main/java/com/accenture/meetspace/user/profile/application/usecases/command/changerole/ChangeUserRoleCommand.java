package com.accenture.meetspace.user.profile.application.usecases.command.changerole;

import com.accenture.meetspace.bootstrap.application.command.Command;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;

import java.util.UUID;

import static com.accenture.meetspace.bootstrap.application.command.CommandPreconditions.requireNonNull;

public record ChangeUserRoleCommand(UUID userId, RoleEnum role) implements Command<ChangeUserRoleResponse> {

    public ChangeUserRoleCommand {
        requireNonNull(userId, "userId");
        requireNonNull(role, "role");
    }
}
