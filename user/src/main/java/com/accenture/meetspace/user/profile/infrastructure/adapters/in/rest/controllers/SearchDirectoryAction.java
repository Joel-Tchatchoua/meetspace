package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.Pagination;
import com.accenture.meetspace.bootstrap.application.query.QueryBus;
import com.accenture.meetspace.user.profile.application.usecases.query.directory.DirectoryEntry;
import com.accenture.meetspace.user.profile.application.usecases.query.directory.SearchDirectoryQuery;
import com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.api.SearchDirectoryApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchDirectoryAction implements SearchDirectoryApi {

    private final QueryBus queryBus;

    public SearchDirectoryAction(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @Override
    public ResponseEntity<PageResponse<DirectoryEntry>> searchDirectory(String name, Pagination pagination) {
        return ResponseEntity.ok(queryBus.ask(new SearchDirectoryQuery(name, pagination)));
    }
}
