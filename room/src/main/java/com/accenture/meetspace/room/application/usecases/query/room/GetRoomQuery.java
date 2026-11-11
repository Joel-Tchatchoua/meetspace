package com.accenture.meetspace.room.application.usecases.query.room;

import com.accenture.meetspace.bootstrap.application.query.InvalidQueryException;
import com.accenture.meetspace.bootstrap.application.query.Query;

import java.util.UUID;

public record GetRoomQuery(UUID roomId) implements Query<RoomView> {

    public GetRoomQuery {
        if (roomId == null) {
            throw new InvalidQueryException("roomId", "must not be null");
        }
    }
}
