package com.accenture.meetspace.room.application.command.update;

import com.accenture.meetspace.room.application.port.SiteRepository;
import com.accenture.meetspace.room.domain.exceptions.SiteCodeAlreadyUsedException;
import com.accenture.meetspace.room.domain.exceptions.SiteNameAlreadyUsedException;
import com.accenture.meetspace.room.domain.exceptions.SiteNotFoundException;
import com.accenture.meetspace.shared.application.command.CommandHandler;

import java.time.ZoneId;

public class UpdateSiteCommandHandler implements CommandHandler<UpdateSiteCommand> {

    private final SiteRepository siteRepository;

    public UpdateSiteCommandHandler(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    public void handle(UpdateSiteCommand command) {
        var site = siteRepository.findById(command.siteId())
                .orElseThrow(() -> new SiteNotFoundException(command.siteId()));
        var current = site.snapshot();
        if (!current.code().equals(command.code()) && siteRepository.existsByCode(command.code())) {
            throw new SiteCodeAlreadyUsedException(command.code());
        }
        if (!current.name().equals(command.name()) && siteRepository.existsByName(command.name())) {
            throw new SiteNameAlreadyUsedException(command.name());
        }
        site.changeDetails(command.code(), command.name(), command.address(), ZoneId.of(command.timeZone()));
        siteRepository.save(site);
    }
}
