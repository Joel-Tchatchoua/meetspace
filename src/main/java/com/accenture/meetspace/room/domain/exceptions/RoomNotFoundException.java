package com.accenture.meetspace.room.domain.exceptions;

import com.accenture.meetspace.room.domain.vo.RoomId;
import com.accenture.meetspace.shared.domain.NotFoundException;

public class RoomNotFoundException extends NotFoundException {

    public RoomNotFoundException(RoomId roomId) {
        super("ROOM_NOT_FOUND", "Room " + roomId + " not found");
    }
}
