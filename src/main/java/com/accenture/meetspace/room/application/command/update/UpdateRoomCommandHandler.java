package com.accenture.meetspace.room.application.command.update;

import com.accenture.meetspace.room.application.port.SiteRepository;
import com.accenture.meetspace.room.domain.exceptions.RoomNotFoundException;
import com.accenture.meetspace.shared.application.command.CommandHandler;

public class UpdateRoomCommandHandler implements CommandHandler<UpdateRoomCommand> {

    private final SiteRepository siteRepository;

    public UpdateRoomCommandHandler(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    public void handle(UpdateRoomCommand command) {
        var site = siteRepository.findByRoomId(command.roomId())
                .orElseThrow(() -> new RoomNotFoundException(command.roomId()));
        site.changeRoomDetails(command.roomId(), command.code(), command.name(), command.floor(), command.capacity(),
                command.amenities());
        siteRepository.save(site);
    }
}
