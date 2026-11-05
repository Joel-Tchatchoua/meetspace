package com.accenture.meetspace.room.infrastructure.adapters.in.rest.requests;

import com.accenture.meetspace.room.application.usecases.command.changestatus.ChangeRoomStatusCommand;
import com.accenture.meetspace.room.domain.enums.RoomStatus;
import com.accenture.meetspace.room.domain.vo.RoomId;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChangeRoomStatusRequest(@NotNull RoomStatus status) {

    public ChangeRoomStatusCommand toCommand(UUID roomId) {
        return new ChangeRoomStatusCommand(RoomId.of(roomId), status);
    }
}
