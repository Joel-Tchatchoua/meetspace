package com.accenture.meetspace.room.application.command.update;

import java.util.UUID;

public record UpdateRoomResponse(UUID roomId, boolean updated) {
}
