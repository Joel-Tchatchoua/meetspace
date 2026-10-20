package com.accenture.meetspace.room.application.usecases.delete;

import com.accenture.meetspace.room.application.port.out.repository.SiteRepository;
import com.accenture.meetspace.room.domain.exceptions.RoomNotFoundException;
import com.accenture.meetspace.shared.application.command.CommandHandler;

public class DeleteRoomCommandHandler
        implements CommandHandler<DeleteRoomCommand, DeleteRoomResponse> {

    private final SiteRepository siteRepository;

    public DeleteRoomCommandHandler(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    public DeleteRoomResponse handle(DeleteRoomCommand command) {
        var site = siteRepository.findByRoomId(command.roomId())
                .orElseThrow(() -> new RoomNotFoundException(command.roomId()));
        site.removeRoom(command.roomId());
        siteRepository.save(site);
        return new DeleteRoomResponse(command.roomId().value(), true);
    }
}
