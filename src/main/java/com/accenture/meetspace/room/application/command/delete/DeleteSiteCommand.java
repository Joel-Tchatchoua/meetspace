package com.accenture.meetspace.room.application.command.delete;

import com.accenture.meetspace.room.domain.vo.SiteId;
import com.accenture.meetspace.shared.application.command.Command;

import static com.accenture.meetspace.shared.application.command.CommandPreconditions.requireNonNull;

public record DeleteSiteCommand(SiteId siteId) implements Command {

    public DeleteSiteCommand {
        requireNonNull(siteId, "siteId");
    }
}
