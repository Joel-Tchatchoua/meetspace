package com.accenture.meetspace.room.application.usecases.delete;

import com.accenture.meetspace.room.application.port.out.repository.SiteRepository;
import com.accenture.meetspace.room.domain.exceptions.SiteNotFoundException;
import com.accenture.meetspace.shared.application.command.CommandHandler;

public class DeleteSiteCommandHandler
        implements CommandHandler<DeleteSiteCommand, DeleteSiteResponse> {

    private final SiteRepository siteRepository;

    public DeleteSiteCommandHandler(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    public DeleteSiteResponse handle(DeleteSiteCommand command) {
        var site = siteRepository.findById(command.siteId())
                .orElseThrow(() -> new SiteNotFoundException(command.siteId()));
        site.delete();
        siteRepository.save(site);
        return new DeleteSiteResponse(command.siteId().value(), true);
    }
}
