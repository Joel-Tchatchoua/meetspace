package com.accenture.meetspace.room.infrastructure.web.requests;

import com.accenture.meetspace.room.application.command.create.CreateSiteCommand;
import com.accenture.meetspace.room.domain.vo.SiteId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSiteRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 255) String address,
        @Size(max = 64) String timeZone) {

    public CreateSiteCommand toCommand() {
        return new CreateSiteCommand(SiteId.generate(), code, name, address, timeZone);
    }
}
