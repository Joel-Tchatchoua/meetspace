package com.accenture.meetspace.room.application.usecases.update;

import com.accenture.meetspace.room.domain.enums.Amenity;
import com.accenture.meetspace.room.domain.vo.Capacity;
import com.accenture.meetspace.room.domain.vo.RoomId;
import com.accenture.meetspace.shared.application.command.Command;

import java.util.Set;

import static com.accenture.meetspace.shared.application.command.CommandPreconditions.copyOrEmpty;
import static com.accenture.meetspace.shared.application.command.CommandPreconditions.requireNonNull;
import static com.accenture.meetspace.shared.application.command.CommandPreconditions.requireNotBlank;

public record UpdateRoomCommand(RoomId roomId, String code, String name, Integer floor, Capacity capacity,
                                Set<Amenity> amenities) implements Command<UpdateRoomResponse> {

    public UpdateRoomCommand {
        requireNonNull(roomId, "roomId");
        requireNotBlank(code, "code");
        requireNotBlank(name, "name");
        requireNonNull(floor, "floor");
        requireNonNull(capacity, "capacity");
        amenities = copyOrEmpty(amenities);
    }
}
