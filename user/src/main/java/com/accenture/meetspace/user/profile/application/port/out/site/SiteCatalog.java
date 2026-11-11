package com.accenture.meetspace.user.profile.application.port.out.site;

import java.util.UUID;

public interface SiteCatalog {

    boolean exists(UUID siteId);
}
