package com.accenture.meetspace.room.application.command.create;

import java.util.UUID;

public record CreateRoomResponse(UUID roomId, boolean created) {
}
