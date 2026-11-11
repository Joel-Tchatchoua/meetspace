package com.accenture.meetspace.user.profile.application.usecases.query.profile;

import com.accenture.meetspace.user.profile.application.usecases.query.user.UserView;
import com.accenture.meetspace.bootstrap.application.query.QueryHandler;
import com.accenture.meetspace.user.profile.application.port.out.reader.UserReader;
import com.accenture.meetspace.user.profile.domain.exceptions.UserNotFoundException;

public class GetProfileQueryHandler implements QueryHandler<GetProfileQuery, UserView> {

    private final UserReader userReader;

    public GetProfileQueryHandler(UserReader userReader) {
        this.userReader = userReader;
    }

    @Override
    public UserView handle(GetProfileQuery query) {
        return userReader.findById(query.userId()).orElseThrow(() -> new UserNotFoundException(query.userId()));
    }
}
