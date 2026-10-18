package com.accenture.meetspace.room.application.command.changestatus;

import com.accenture.meetspace.room.domain.enums.RoomStatus;
import com.accenture.meetspace.room.domain.vo.RoomId;
import com.accenture.meetspace.shared.application.command.Command;

import static com.accenture.meetspace.shared.application.command.CommandPreconditions.requireNonNull;

public record ChangeRoomStatusCommand(RoomId roomId, RoomStatus status) implements Command {

    public ChangeRoomStatusCommand {
        requireNonNull(roomId, "roomId");
        requireNonNull(status, "status");
    }
}
