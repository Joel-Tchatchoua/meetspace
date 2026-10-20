package com.accenture.meetspace.room.application.usecases.create;

import com.accenture.meetspace.room.application.port.out.repository.SiteRepository;
import com.accenture.meetspace.room.domain.exceptions.SiteNotFoundException;
import com.accenture.meetspace.shared.application.command.CommandHandler;

public class CreateRoomCommandHandler
        implements CommandHandler<CreateRoomCommand, CreateRoomResponse> {

    private final SiteRepository siteRepository;

    public CreateRoomCommandHandler(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    public CreateRoomResponse handle(CreateRoomCommand command) {
        var site = siteRepository.findById(command.siteId())
                .orElseThrow(() -> new SiteNotFoundException(command.siteId()));
        site.addRoom(command.roomId(), command.code(), command.name(), command.floor(), command.capacity(),
                command.amenities());
        siteRepository.save(site);
        return new CreateRoomResponse(command.roomId().value(), true);
    }
}
