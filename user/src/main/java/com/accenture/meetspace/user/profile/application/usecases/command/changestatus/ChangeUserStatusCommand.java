package com.accenture.meetspace.user.profile.application.usecases.command.changestatus;

import com.accenture.meetspace.bootstrap.application.command.Command;

import java.util.UUID;

import static com.accenture.meetspace.bootstrap.application.command.CommandPreconditions.requireNonNull;

public record ChangeUserStatusCommand(UUID actorId, UUID userId, boolean active)
        implements Command<ChangeUserStatusResponse> {

    public ChangeUserStatusCommand {
        requireNonNull(actorId, "actorId");
        requireNonNull(userId, "userId");
    }
}
