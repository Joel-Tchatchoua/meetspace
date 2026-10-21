package com.accenture.meetspace.room.infrastructure.config;

import com.accenture.meetspace.room.application.usecases.query.GetSiteQuery;
import com.accenture.meetspace.room.application.usecases.query.GetSiteQueryHandler;
import com.accenture.meetspace.room.application.usecases.query.ListSitesQuery;
import com.accenture.meetspace.room.application.usecases.query.ListSitesQueryHandler;
import com.accenture.meetspace.room.application.usecases.query.SiteView;
import com.accenture.meetspace.shared.application.query.PageResponse;
import com.accenture.meetspace.shared.application.query.QueryHandlerRegistration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration(proxyBeanMethods = false)
public class RoomQueryHandlersConfiguration {

    @Bean
    QueryHandlerRegistration<GetSiteQuery, SiteView> getSite(NamedParameterJdbcTemplate jdbc) {
        return new QueryHandlerRegistration<>(GetSiteQuery.class, new GetSiteQueryHandler(jdbc));
    }

    @Bean
    QueryHandlerRegistration<ListSitesQuery, PageResponse<SiteView>> listSites(NamedParameterJdbcTemplate jdbc) {
        return new QueryHandlerRegistration<>(ListSitesQuery.class, new ListSitesQueryHandler(jdbc));
    }
}
