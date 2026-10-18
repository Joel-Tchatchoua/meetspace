package com.accenture.meetspace.room.application.command.create;

import com.accenture.meetspace.room.domain.enums.Amenity;
import com.accenture.meetspace.room.domain.vo.Capacity;
import com.accenture.meetspace.room.domain.vo.RoomId;
import com.accenture.meetspace.room.domain.vo.SiteId;
import com.accenture.meetspace.shared.application.command.Command;

import java.util.Set;

import static com.accenture.meetspace.shared.application.command.CommandPreconditions.copyOrEmpty;
import static com.accenture.meetspace.shared.application.command.CommandPreconditions.requireNonNull;
import static com.accenture.meetspace.shared.application.command.CommandPreconditions.requireNotBlank;

public record CreateRoomCommand(RoomId roomId, SiteId siteId, String code, String name, Integer floor,
                                Capacity capacity, Set<Amenity> amenities) implements Command {

    public CreateRoomCommand {
        requireNonNull(roomId, "roomId");
        requireNonNull(siteId, "siteId");
        requireNotBlank(code, "code");
        requireNotBlank(name, "name");
        requireNonNull(floor, "floor");
        requireNonNull(capacity, "capacity");
        amenities = copyOrEmpty(amenities);
    }
}
