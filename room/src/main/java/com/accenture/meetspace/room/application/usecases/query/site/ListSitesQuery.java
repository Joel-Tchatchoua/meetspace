package com.accenture.meetspace.room.application.usecases.query.site;

import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.PaginatedQuery;
import com.accenture.meetspace.bootstrap.application.query.Pagination;
import com.accenture.meetspace.bootstrap.application.query.SortOrder;

import java.util.Map;

public record ListSitesQuery(Pagination pagination) implements PaginatedQuery<PageResponse<SiteView>> {

    private static final Map<String, String> SORTABLE_COLUMNS = Map.of(
            "code", "code",
            "name", "name",
            "address", "address",
            "timeZone", "time_zone");

    private static final SortOrder DEFAULT_SORT = new SortOrder("code", true);

    public ListSitesQuery {
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
