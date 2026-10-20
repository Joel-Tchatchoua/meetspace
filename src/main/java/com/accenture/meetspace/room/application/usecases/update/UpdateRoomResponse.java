package com.accenture.meetspace.room.application.usecases.update;

import java.util.UUID;

public record UpdateRoomResponse(UUID roomId, boolean updated) {
}
