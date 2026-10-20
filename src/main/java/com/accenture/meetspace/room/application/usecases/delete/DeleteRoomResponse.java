package com.accenture.meetspace.room.application.usecases.delete;

import java.util.UUID;

public record DeleteRoomResponse(UUID roomId, boolean deleted) {
}
