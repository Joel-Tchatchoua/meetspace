package com.accenture.meetspace.room.application.usecases.delete;

import java.util.UUID;

public record DeleteSiteResponse(UUID siteId, boolean deleted) {
}
