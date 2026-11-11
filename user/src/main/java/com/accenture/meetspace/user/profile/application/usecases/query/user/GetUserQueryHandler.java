package com.accenture.meetspace.user.profile.application.usecases.query.user;

import com.accenture.meetspace.bootstrap.application.query.QueryHandler;
import com.accenture.meetspace.user.profile.application.port.out.reader.UserReader;
import com.accenture.meetspace.user.profile.domain.exceptions.UserNotFoundException;

public class GetUserQueryHandler implements QueryHandler<GetUserQuery, UserView> {

    private final UserReader userReader;

    public GetUserQueryHandler(UserReader userReader) {
        this.userReader = userReader;
    }

    @Override
    public UserView handle(GetUserQuery query) {
        return userReader.findById(query.userId()).orElseThrow(() -> new UserNotFoundException(query.userId()));
    }
}
