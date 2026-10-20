package com.accenture.meetspace.room.application.usecases.changestatus;

import com.accenture.meetspace.room.domain.enums.RoomStatus;

import java.util.UUID;

public record ChangeRoomStatusResponse(UUID roomId, RoomStatus status, boolean changed) {
}
