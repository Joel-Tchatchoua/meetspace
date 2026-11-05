package com.accenture.meetspace.room.application.usecases.command.delete;

import com.accenture.meetspace.room.domain.vo.RoomId;
import com.accenture.meetspace.bootstrap.application.command.Command;

import static com.accenture.meetspace.bootstrap.application.command.CommandPreconditions.requireNonNull;

public record DeleteRoomCommand(RoomId roomId) implements Command<DeleteRoomResponse> {

    public DeleteRoomCommand {
        requireNonNull(roomId, "roomId");
    }
}
