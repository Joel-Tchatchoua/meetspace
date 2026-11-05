package com.accenture.meetspace.room.application.usecases.command.delete;

import java.util.UUID;

public record DeleteSiteResponse(UUID siteId, boolean deleted) {
}
