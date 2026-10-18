package com.accenture.meetspace.room.application.command.delete;

import com.accenture.meetspace.room.domain.vo.RoomId;
import com.accenture.meetspace.shared.application.command.Command;

import static com.accenture.meetspace.shared.application.command.CommandPreconditions.requireNonNull;

public record DeleteRoomCommand(RoomId roomId) implements Command {

    public DeleteRoomCommand {
        requireNonNull(roomId, "roomId");
    }
}
