package com.accenture.meetspace.room.domain.exceptions;

import com.accenture.meetspace.room.domain.vo.SiteId;
import com.accenture.meetspace.shared.domain.BusinessRuleViolationException;

public class SiteHasRoomsException extends BusinessRuleViolationException {

    public SiteHasRoomsException(SiteId siteId) {
        super("SITE_HAS_ROOMS", "Site " + siteId + " still has rooms");
    }
}
