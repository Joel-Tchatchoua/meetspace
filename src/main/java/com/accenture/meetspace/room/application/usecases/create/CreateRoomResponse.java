package com.accenture.meetspace.room.application.usecases.create;

import java.util.UUID;

public record CreateRoomResponse(UUID roomId, boolean created) {
}
