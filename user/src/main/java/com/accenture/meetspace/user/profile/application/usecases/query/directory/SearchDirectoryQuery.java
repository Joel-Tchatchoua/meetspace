package com.accenture.meetspace.user.profile.application.usecases.query.directory;

import com.accenture.meetspace.bootstrap.application.query.InvalidQueryException;
import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.PaginatedQuery;
import com.accenture.meetspace.bootstrap.application.query.Pagination;
import com.accenture.meetspace.bootstrap.application.query.SortOrder;

import java.util.Map;

public record SearchDirectoryQuery(String name, Pagination pagination)
        implements PaginatedQuery<PageResponse<DirectoryEntry>> {

    private static final Map<String, String> SORTABLE_COLUMNS = Map.of(
            "username", "u.username",
            "firstName", "u.first_name",
            "lastName", "u.last_name");

    private static final SortOrder DEFAULT_SORT = new SortOrder("lastName", true);

    public SearchDirectoryQuery {
        if (name == null || name.isBlank()) {
            throw new InvalidQueryException("name", "must not be blank");
        }
        pagination = pagination == null ? Pagination.DEFAULT : pagination;
    }

    @Override
    public Map<String, String> sortableColumns() {
        return SORTABLE_COLUMNS;
    }

    @Override
    public SortOrder defaultSort() {
        return DEFAULT_SORT;
    }
}
