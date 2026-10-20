package com.accenture.meetspace.room.application.command.delete;

import java.util.UUID;

public record DeleteSiteResponse(UUID siteId, boolean deleted) {
}
