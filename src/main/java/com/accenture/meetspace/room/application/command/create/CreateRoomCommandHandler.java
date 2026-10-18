package com.accenture.meetspace.room.application.command.create;

import com.accenture.meetspace.room.application.port.SiteRepository;
import com.accenture.meetspace.room.domain.exceptions.SiteNotFoundException;
import com.accenture.meetspace.shared.application.command.CommandHandler;

public class CreateRoomCommandHandler implements CommandHandler<CreateRoomCommand> {

    private final SiteRepository siteRepository;

    public CreateRoomCommandHandler(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    public void handle(CreateRoomCommand command) {
        var site = siteRepository.findById(command.siteId())
                .orElseThrow(() -> new SiteNotFoundException(command.siteId()));
        site.addRoom(command.roomId(), command.code(), command.name(), command.floor(), command.capacity(),
                command.amenities());
        siteRepository.save(site);
    }
}
