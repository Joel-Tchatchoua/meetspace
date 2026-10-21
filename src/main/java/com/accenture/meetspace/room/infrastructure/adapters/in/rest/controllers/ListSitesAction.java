package com.accenture.meetspace.room.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.room.application.usecases.query.ListSitesQuery;
import com.accenture.meetspace.room.application.usecases.query.SiteView;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.api.ListSitesApi;
import com.accenture.meetspace.shared.application.query.PageResponse;
import com.accenture.meetspace.shared.application.query.Pagination;
import com.accenture.meetspace.shared.application.query.QueryBus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListSitesAction implements ListSitesApi {

    private final QueryBus queryBus;

    public ListSitesAction(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @Override
    public ResponseEntity<PageResponse<SiteView>> listSites(Pagination pagination) {
        return ResponseEntity.ok(queryBus.ask(new ListSitesQuery(pagination)));
    }
}
