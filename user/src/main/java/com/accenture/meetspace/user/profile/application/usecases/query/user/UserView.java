package com.accenture.meetspace.user.profile.application.usecases.query.user;

import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;

import java.util.UUID;

public record UserView(UUID id, String username, String email, String firstName, String lastName, String department,
                       String phone, UUID defaultSiteId, RoleEnum role, boolean active) {
}
