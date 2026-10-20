package com.accenture.meetspace.room.application.usecases.create;

import java.util.UUID;

public record CreateSiteResponse(UUID siteId, boolean created) {
}
