package com.accenture.meetspace.room.application.command.create;

import com.accenture.meetspace.room.domain.entities.Site;
import com.accenture.meetspace.room.domain.vo.SiteId;
import com.accenture.meetspace.shared.application.command.Command;

import static com.accenture.meetspace.shared.application.command.CommandPreconditions.requireNonNull;
import static com.accenture.meetspace.shared.application.command.CommandPreconditions.requireNotBlank;
import static com.accenture.meetspace.shared.application.command.CommandPreconditions.requireTimeZoneOrDefault;

public record CreateSiteCommand(SiteId siteId, String code, String name, String address, String timeZone)
        implements Command {

    public CreateSiteCommand {
        requireNonNull(siteId, "siteId");
        requireNotBlank(code, "code");
        requireNotBlank(name, "name");
        requireNotBlank(address, "address");
        timeZone = requireTimeZoneOrDefault(timeZone, Site.DEFAULT_TIME_ZONE, "timeZone");
    }
}
