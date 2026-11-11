package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.bootstrap.application.query.QueryBus;
import com.accenture.meetspace.user.profile.application.usecases.query.profile.GetProfileQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.user.UserView;
import com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.api.GetProfileApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class GetProfileAction implements GetProfileApi {

    private final QueryBus queryBus;

    public GetProfileAction(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @Override
    public ResponseEntity<UserView> getProfile(UUID userId) {
        return ResponseEntity.ok(queryBus.ask(new GetProfileQuery(userId)));
    }
}
