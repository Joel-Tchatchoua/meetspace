package com.accenture.meetspace.room.domain.exceptions;

import com.accenture.meetspace.room.domain.vo.SiteId;
import com.accenture.meetspace.shared.domain.NotFoundException;

public class SiteNotFoundException extends NotFoundException {

    public SiteNotFoundException(SiteId siteId) {
        super("SITE_NOT_FOUND", "Site " + siteId + " not found");
    }
}
