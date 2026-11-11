package com.accenture.meetspace.user.profile.application.usecases.command.changestatus;

import java.util.UUID;

public record ChangeUserStatusResponse(UUID userId, boolean active) {
}
