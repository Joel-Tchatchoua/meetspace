package com.accenture.meetspace.room.domain.exceptions;

import com.accenture.meetspace.room.domain.vo.SiteId;
import com.accenture.meetspace.shared.domain.BusinessRuleViolationException;

public class RoomCodeAlreadyUsedException extends BusinessRuleViolationException {

    public RoomCodeAlreadyUsedException(SiteId siteId, String roomCode) {
        super("ROOM_CODE_ALREADY_USED", "A room with code " + roomCode + " already exists in site " + siteId);
    }
}
