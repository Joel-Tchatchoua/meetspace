package com.accenture.meetspace.room.infrastructure.adapters.in.rest.requests;

import com.accenture.meetspace.room.application.usecases.command.update.UpdateSiteCommand;
import com.accenture.meetspace.room.domain.vo.SiteId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateSiteRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 255) String address,
        @Size(max = 64) String timeZone) {

    public UpdateSiteCommand toCommand(UUID siteId) {
        return new UpdateSiteCommand(SiteId.of(siteId), code, name, address, timeZone);
    }
}
