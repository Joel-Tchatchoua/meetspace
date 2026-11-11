package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.Pagination;
import com.accenture.meetspace.bootstrap.application.query.QueryBus;
import com.accenture.meetspace.user.profile.application.usecases.query.user.ListUsersQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.user.UserView;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.api.ListUsersApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListUsersAction implements ListUsersApi {

    private final QueryBus queryBus;

    public ListUsersAction(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @Override
    public ResponseEntity<PageResponse<UserView>> listUsers(String username, String email, String department,
                                                            RoleEnum role, Boolean active, Pagination pagination) {
        return ResponseEntity.ok(queryBus.ask(
                new ListUsersQuery(username, email, department, role, active, pagination)));
    }
}
