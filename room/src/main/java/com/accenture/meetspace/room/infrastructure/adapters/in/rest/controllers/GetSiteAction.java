package com.accenture.meetspace.room.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.room.application.usecases.query.GetSiteQuery;
import com.accenture.meetspace.room.application.usecases.query.SiteView;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.api.GetSiteApi;
import com.accenture.meetspace.bootstrap.application.query.QueryBus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class GetSiteAction implements GetSiteApi {

    private final QueryBus queryBus;

    public GetSiteAction(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @Override
    public ResponseEntity<SiteView> getSite(UUID id) {
        return ResponseEntity.ok(queryBus.ask(new GetSiteQuery(id)));
    }
}
