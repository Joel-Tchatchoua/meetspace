package com.accenture.meetspace.user.profile.application.usecases.query.profile;

import com.accenture.meetspace.user.profile.application.usecases.query.user.UserView;
import com.accenture.meetspace.bootstrap.application.query.InvalidQueryException;
import com.accenture.meetspace.bootstrap.application.query.Query;

import java.util.UUID;

public record GetProfileQuery(UUID userId) implements Query<UserView> {

    public GetProfileQuery {
        if (userId == null) {
            throw new InvalidQueryException("userId", "must not be null");
        }
    }
}
