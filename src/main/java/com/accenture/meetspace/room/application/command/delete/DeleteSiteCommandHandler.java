package com.accenture.meetspace.room.application.command.delete;

import com.accenture.meetspace.room.application.port.out.SiteRepository;
import com.accenture.meetspace.room.domain.exceptions.SiteNotFoundException;
import com.accenture.meetspace.shared.application.command.CommandHandler;

public class DeleteSiteCommandHandler implements CommandHandler<DeleteSiteCommand> {

    private final SiteRepository siteRepository;

    public DeleteSiteCommandHandler(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    public void handle(DeleteSiteCommand command) {
        var site = siteRepository.findById(command.siteId())
                .orElseThrow(() -> new SiteNotFoundException(command.siteId()));
        site.delete();
        siteRepository.save(site);
    }
}
