package com.accenture.meetspace.room.application.usecases.query;

import com.accenture.meetspace.room.domain.exceptions.SiteNotFoundException;
import com.accenture.meetspace.room.domain.vo.SiteId;
import com.accenture.meetspace.bootstrap.application.query.QueryHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

public class GetSiteQueryHandler implements QueryHandler<GetSiteQuery, SiteView> {

    private static final String SITE = "select " + SiteView.COLUMNS + """
             from sites
            where id = :id
              and deleted_at is null
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public GetSiteQueryHandler(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public SiteView handle(GetSiteQuery query) {
        return jdbc.query(SITE, Map.of("id", query.siteId().toString()), SiteView::fromRow).stream()
                .findFirst()
                .orElseThrow(() -> new SiteNotFoundException(SiteId.of(query.siteId())));
    }
}
