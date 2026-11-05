package com.accenture.meetspace.room.application.usecases.command.delete;

import com.accenture.meetspace.room.domain.vo.SiteId;
import com.accenture.meetspace.bootstrap.application.command.Command;

import static com.accenture.meetspace.bootstrap.application.command.CommandPreconditions.requireNonNull;

public record DeleteSiteCommand(SiteId siteId) implements Command<DeleteSiteResponse> {

    public DeleteSiteCommand {
        requireNonNull(siteId, "siteId");
    }
}
