package com.accenture.meetspace.room.application.usecases.query;

import com.accenture.meetspace.shared.application.query.InvalidQueryException;
import com.accenture.meetspace.shared.application.query.Query;

import java.util.UUID;

public record GetSiteQuery(UUID siteId) implements Query<SiteView> {

    public GetSiteQuery {
        if (siteId == null) {
            throw new InvalidQueryException("siteId", "must not be null");
        }
    }
}
