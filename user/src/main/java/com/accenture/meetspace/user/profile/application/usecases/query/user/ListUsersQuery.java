package com.accenture.meetspace.user.profile.application.usecases.query.user;

import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.PaginatedQuery;
import com.accenture.meetspace.bootstrap.application.query.Pagination;
import com.accenture.meetspace.bootstrap.application.query.SortOrder;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;

import java.util.Map;

public record ListUsersQuery(String username, String email, String department, RoleEnum role, Boolean active,
                             Pagination pagination) implements PaginatedQuery<PageResponse<UserView>> {

    private static final Map<String, String> SORTABLE_COLUMNS = Map.of(
            "username", "u.username",
            "email", "u.email",
            "firstName", "u.first_name",
            "lastName", "u.last_name",
            "department", "u.department",
            "role", "u.role",
            "active", "u.active");

    private static final SortOrder DEFAULT_SORT = new SortOrder("username", true);

    public ListUsersQuery {
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
