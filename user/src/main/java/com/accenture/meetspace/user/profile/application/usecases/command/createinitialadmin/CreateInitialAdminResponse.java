package com.accenture.meetspace.user.profile.application.usecases.command.createinitialadmin;

import java.util.UUID;

public record CreateInitialAdminResponse(UUID userId, boolean created) {
}
