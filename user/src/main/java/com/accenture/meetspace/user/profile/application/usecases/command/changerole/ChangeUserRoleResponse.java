package com.accenture.meetspace.user.profile.application.usecases.command.changerole;

import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;

import java.util.UUID;

public record ChangeUserRoleResponse(UUID userId, RoleEnum role) {
}
