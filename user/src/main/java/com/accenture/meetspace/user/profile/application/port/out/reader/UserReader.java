package com.accenture.meetspace.user.profile.application.port.out.reader;

import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.user.profile.application.usecases.query.directory.DirectoryEntry;
import com.accenture.meetspace.user.profile.application.usecases.query.user.ListUsersQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.directory.SearchDirectoryQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.user.UserView;

import java.util.Optional;
import java.util.UUID;

public interface UserReader {

    Optional<UserView> findById(UUID id);

    PageResponse<UserView> search(ListUsersQuery query);

    PageResponse<DirectoryEntry> searchDirectory(SearchDirectoryQuery query);
}
