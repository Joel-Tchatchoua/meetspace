package com.accenture.meetspace.room.application.usecases.command.delete;

import java.util.UUID;

public record DeleteRoomResponse(UUID roomId, boolean deleted) {
}
