package com.accenture.meetspace.room.application.command.update;

import java.util.UUID;

public record UpdateSiteResponse(UUID siteId, boolean updated) {
}
