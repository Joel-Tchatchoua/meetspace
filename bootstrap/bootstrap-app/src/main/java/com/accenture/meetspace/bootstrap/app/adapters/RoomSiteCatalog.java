package com.accenture.meetspace.bootstrap.app.adapters;

import com.accenture.meetspace.bootstrap.application.query.QueryBus;
import com.accenture.meetspace.room.application.usecases.query.site.GetSiteQuery;
import com.accenture.meetspace.room.domain.exceptions.SiteNotFoundException;
import com.accenture.meetspace.user.profile.application.port.out.site.SiteCatalog;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RoomSiteCatalog implements SiteCatalog {

    private final QueryBus queryBus;

    public RoomSiteCatalog(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @Override
    public boolean exists(UUID siteId) {
        try {
            queryBus.ask(new GetSiteQuery(siteId));
            return true;
        } catch (SiteNotFoundException e) {
            return false;
        }
    }
}
