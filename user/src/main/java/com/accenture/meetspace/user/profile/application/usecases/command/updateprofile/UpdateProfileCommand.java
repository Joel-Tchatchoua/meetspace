package com.accenture.meetspace.user.profile.application.usecases.command.updateprofile;

import com.accenture.meetspace.bootstrap.application.command.Command;

import java.util.UUID;

import static com.accenture.meetspace.bootstrap.application.command.CommandPreconditions.requireNonNull;
import static com.accenture.meetspace.bootstrap.application.command.CommandPreconditions.requireNotBlank;

public record UpdateProfileCommand(UUID userId, String firstName, String lastName, String department, String phone,
                                   UUID defaultSiteId) implements Command<UpdateProfileResponse> {

    public UpdateProfileCommand {
        requireNonNull(userId, "userId");
        if (firstName != null) {
            requireNotBlank(firstName, "firstName");
        }
        if (lastName != null) {
            requireNotBlank(lastName, "lastName");
        }
    }
}
