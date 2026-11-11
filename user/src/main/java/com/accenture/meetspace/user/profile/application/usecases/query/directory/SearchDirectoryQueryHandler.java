package com.accenture.meetspace.user.profile.application.usecases.query.directory;

import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.QueryHandler;
import com.accenture.meetspace.user.profile.application.port.out.reader.UserReader;

public class SearchDirectoryQueryHandler implements QueryHandler<SearchDirectoryQuery, PageResponse<DirectoryEntry>> {

    private final UserReader userReader;

    public SearchDirectoryQueryHandler(UserReader userReader) {
        this.userReader = userReader;
    }

    @Override
    public PageResponse<DirectoryEntry> handle(SearchDirectoryQuery query) {
        return userReader.searchDirectory(query);
    }
}
