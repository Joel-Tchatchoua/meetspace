package com.accenture.meetspace.user.profile.application.port.out.identity;

import java.util.UUID;

public record IdentityUser(UUID keycloakUserId, String username, String email, String firstName, String lastName) {
}
