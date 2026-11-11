package com.accenture.meetspace.user.profile.application.usecases.query.user;

import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.QueryHandler;
import com.accenture.meetspace.user.profile.application.port.out.reader.UserReader;

public class ListUsersQueryHandler implements QueryHandler<ListUsersQuery, PageResponse<UserView>> {

    private final UserReader userReader;

    public ListUsersQueryHandler(UserReader userReader) {
        this.userReader = userReader;
    }

    @Override
    public PageResponse<UserView> handle(ListUsersQuery query) {
        return userReader.search(query);
    }
}
