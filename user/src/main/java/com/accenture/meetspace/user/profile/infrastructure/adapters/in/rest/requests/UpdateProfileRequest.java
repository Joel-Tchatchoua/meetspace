package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.requests;

import com.accenture.meetspace.user.profile.application.usecases.command.updateprofile.UpdateProfileCommand;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateProfileRequest(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Size(max = 100) String department,
        @Size(max = 30) String phone,
        UUID defaultSiteId) {

    public UpdateProfileCommand toCommand(UUID userId) {
        return new UpdateProfileCommand(userId, firstName, lastName, department, phone, defaultSiteId);
    }
}
