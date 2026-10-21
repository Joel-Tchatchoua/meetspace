package com.accenture.meetspace.room.application.usecases.query;

import com.accenture.meetspace.shared.application.query.PageResponse;
import com.accenture.meetspace.shared.application.query.QueryHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

public class ListSitesQueryHandler implements QueryHandler<ListSitesQuery, PageResponse<SiteView>> {

    private static final String COUNT = """
            select count(*)
             from sites
            where deleted_at is null
            """;

    private static final String PAGE = "select " + SiteView.COLUMNS + """
             from sites
            where deleted_at is null
            order by %s
            limit :limit offset :offset
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public ListSitesQueryHandler(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public PageResponse<SiteView> handle(ListSitesQuery query) {
        var pagination = query.pagination();
        var total = jdbc.queryForObject(COUNT, Map.of(), Long.class);
        var content = jdbc.query(PAGE.formatted(query.orderBy("id")),
                Map.of("limit", pagination.size(), "offset", pagination.offset()),
                SiteView::fromRow);
        return PageResponse.of(content, pagination, total == null ? 0 : total);
    }
}
