package com.accenture.meetspace.room.application.command.changestatus;

import com.accenture.meetspace.room.application.port.out.SiteRepository;
import com.accenture.meetspace.room.domain.exceptions.RoomNotFoundException;
import com.accenture.meetspace.shared.application.command.CommandHandler;

public class ChangeRoomStatusCommandHandler
        implements CommandHandler<ChangeRoomStatusCommand, ChangeRoomStatusResponse> {

    private final SiteRepository siteRepository;

    public ChangeRoomStatusCommandHandler(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    public ChangeRoomStatusResponse handle(ChangeRoomStatusCommand command) {
        var site = siteRepository.findByRoomId(command.roomId())
                .orElseThrow(() -> new RoomNotFoundException(command.roomId()));
        switch (command.status()) {
            case ACTIVE -> site.activateRoom(command.roomId());
            case MAINTENANCE -> site.putRoomUnderMaintenance(command.roomId());
            case CLOSED -> site.closeRoom(command.roomId());
        }
        siteRepository.save(site);
        return new ChangeRoomStatusResponse(command.roomId().value(), command.status(), true);
    }
}
