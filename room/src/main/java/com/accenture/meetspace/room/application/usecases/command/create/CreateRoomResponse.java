package com.accenture.meetspace.room.application.usecases.command.create;

import java.util.UUID;

public record CreateRoomResponse(UUID roomId, boolean created) {
}
