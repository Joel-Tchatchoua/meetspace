package com.accenture.meetspace.user.profile.application.usecases.query.user;

import com.accenture.meetspace.bootstrap.application.query.InvalidQueryException;
import com.accenture.meetspace.bootstrap.application.query.Query;

import java.util.UUID;

public record GetUserQuery(UUID userId) implements Query<UserView> {

    public GetUserQuery {
        if (userId == null) {
            throw new InvalidQueryException("userId", "must not be null");
        }
    }
}
