package com.accenture.meetspace.user.profile.infrastructure.adapters.out.site;

import com.accenture.meetspace.user.profile.application.port.out.site.SiteCatalog;
import com.accenture.meetspace.user.profile.domain.exceptions.ExternalServiceUnavailableException;

import java.util.UUID;

public class UnavailableSiteCatalog implements SiteCatalog {

    @Override
    public boolean exists(UUID siteId) {
        throw new ExternalServiceUnavailableException("The room module");
    }
}
