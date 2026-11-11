package com.accenture.meetspace.user.profile.domain.exceptions;

import com.accenture.meetspace.bootstrap.domain.NotFoundException;

import java.util.UUID;

public class DefaultSiteNotFoundException extends NotFoundException {

    public DefaultSiteNotFoundException(UUID siteId) {
        super("DEFAULT_SITE_NOT_FOUND", "Default site " + siteId + " not found");
    }
}
