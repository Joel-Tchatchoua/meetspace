package com.accenture.meetspace.user.profile.application.usecases.query.directory;

import java.util.UUID;

public record DirectoryEntry(UUID userId, String username, String firstName, String lastName) {
}
