package com.accenture.meetspace.room.application.usecases.update;

import java.util.UUID;

public record UpdateSiteResponse(UUID siteId, boolean updated) {
}
