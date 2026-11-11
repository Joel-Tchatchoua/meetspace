package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.bootstrap.application.query.QueryBus;
import com.accenture.meetspace.user.profile.application.usecases.query.user.GetUserQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.user.UserView;
import com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.api.GetUserApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class GetUserAction implements GetUserApi {

    private final QueryBus queryBus;

    public GetUserAction(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @Override
    public ResponseEntity<UserView> getUser(UUID id) {
        return ResponseEntity.ok(queryBus.ask(new GetUserQuery(id)));
    }
}
