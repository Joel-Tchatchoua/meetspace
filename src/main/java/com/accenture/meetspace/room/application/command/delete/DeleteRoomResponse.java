package com.accenture.meetspace.room.application.command.delete;

import java.util.UUID;

public record DeleteRoomResponse(UUID roomId, boolean deleted) {
}
