package com.accenture.meetspace.room.application.usecases.query;

import com.accenture.meetspace.shared.application.query.InvalidQueryException;
import com.accenture.meetspace.shared.application.query.Query;

import java.util.UUID;

public record GetRoomQuery(UUID roomId) implements Query<RoomView> {

    public GetRoomQuery {
        if (roomId == null) {
            throw new InvalidQueryException("roomId", "must not be null");
        }
    }
}
