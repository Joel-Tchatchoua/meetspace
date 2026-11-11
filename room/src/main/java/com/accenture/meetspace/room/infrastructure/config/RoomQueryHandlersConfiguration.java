package com.accenture.meetspace.room.infrastructure.config;

import com.accenture.meetspace.room.application.usecases.query.room.GetRoomQuery;
import com.accenture.meetspace.room.application.usecases.query.room.GetRoomQueryHandler;
import com.accenture.meetspace.room.application.usecases.query.site.GetSiteQuery;
import com.accenture.meetspace.room.application.usecases.query.site.GetSiteQueryHandler;
import com.accenture.meetspace.room.application.usecases.query.room.ListRoomsQuery;
import com.accenture.meetspace.room.application.usecases.query.room.ListRoomsQueryHandler;
import com.accenture.meetspace.room.application.usecases.query.site.ListSitesQuery;
import com.accenture.meetspace.room.application.usecases.query.site.ListSitesQueryHandler;
import com.accenture.meetspace.room.application.usecases.query.room.RoomView;
import com.accenture.meetspace.room.application.usecases.query.site.SiteView;
import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.QueryHandlerRegistration;
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

    @Bean
    QueryHandlerRegistration<GetRoomQuery, RoomView> getRoom(NamedParameterJdbcTemplate jdbc) {
        return new QueryHandlerRegistration<>(GetRoomQuery.class, new GetRoomQueryHandler(jdbc));
    }

    @Bean
    QueryHandlerRegistration<ListRoomsQuery, PageResponse<RoomView>> listRooms(NamedParameterJdbcTemplate jdbc) {
        return new QueryHandlerRegistration<>(ListRoomsQuery.class, new ListRoomsQueryHandler(jdbc));
    }
}
