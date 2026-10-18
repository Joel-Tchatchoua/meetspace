package com.accenture.meetspace.room.domain.vo;

import com.accenture.meetspace.shared.domain.Id;

import java.util.UUID;

public final class RoomId extends Id {

    private RoomId(UUID value) {
        super(value);
    }

    public static RoomId generate() {
        return new RoomId(randomValue());
    }

    public static RoomId of(UUID value) {
        return new RoomId(value);
    }
}
