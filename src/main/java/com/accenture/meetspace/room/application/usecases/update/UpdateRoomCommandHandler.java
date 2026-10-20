package com.accenture.meetspace.room.application.usecases.update;

import com.accenture.meetspace.room.application.port.out.repository.SiteRepository;
import com.accenture.meetspace.room.domain.exceptions.RoomNotFoundException;
import com.accenture.meetspace.shared.application.command.CommandHandler;

public class UpdateRoomCommandHandler
        implements CommandHandler<UpdateRoomCommand, UpdateRoomResponse> {

    private final SiteRepository siteRepository;

    public UpdateRoomCommandHandler(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    public UpdateRoomResponse handle(UpdateRoomCommand command) {
        var site = siteRepository.findByRoomId(command.roomId())
                .orElseThrow(() -> new RoomNotFoundException(command.roomId()));
        site.changeRoomDetails(command.roomId(), command.code(), command.name(), command.floor(), command.capacity(),
                command.amenities());
        siteRepository.save(site);
        return new UpdateRoomResponse(command.roomId().value(), true);
    }
}
