package com.accenture.meetspace.room.application.usecases.command.update;

import java.util.UUID;

public record UpdateRoomResponse(UUID roomId, boolean updated) {
}
