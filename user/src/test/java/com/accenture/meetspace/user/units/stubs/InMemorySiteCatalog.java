package com.accenture.meetspace.user.units.stubs;

import com.accenture.meetspace.user.profile.application.port.out.site.SiteCatalog;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class InMemorySiteCatalog implements SiteCatalog {

    private final Set<UUID> sites = new HashSet<>();

    @Override
    public boolean exists(UUID siteId) {
        return sites.contains(siteId);
    }

    public void add(UUID siteId) {
        sites.add(siteId);
    }
}
