package com.accenture.meetspace.room.application.command.create;

import com.accenture.meetspace.room.application.port.out.SiteRepository;
import com.accenture.meetspace.room.domain.entities.Site;
import com.accenture.meetspace.room.domain.exceptions.SiteCodeAlreadyUsedException;
import com.accenture.meetspace.room.domain.exceptions.SiteNameAlreadyUsedException;
import com.accenture.meetspace.shared.application.command.CommandHandler;

import java.time.ZoneId;

public class CreateSiteCommandHandler
        implements CommandHandler<CreateSiteCommand, CreateSiteResponse> {

    private final SiteRepository siteRepository;

    public CreateSiteCommandHandler(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    public CreateSiteResponse handle(CreateSiteCommand command) {
        if (siteRepository.existsByCode(command.code())) {
            throw new SiteCodeAlreadyUsedException(command.code());
        }
        if (siteRepository.existsByName(command.name())) {
            throw new SiteNameAlreadyUsedException(command.name());
        }
        siteRepository.save(Site.create(
                command.siteId(), command.code(), command.name(), command.address(), ZoneId.of(command.timeZone())));
        return new CreateSiteResponse(command.siteId().value(), true);
    }
}
