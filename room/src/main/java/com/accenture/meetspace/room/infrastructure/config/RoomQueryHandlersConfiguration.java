package com.accenture.meetspace.room.infrastructure.config;

import com.accenture.meetspace.room.application.usecases.query.GetRoomQuery;
import com.accenture.meetspace.room.application.usecases.query.GetRoomQueryHandler;
import com.accenture.meetspace.room.application.usecases.query.GetSiteQuery;
import com.accenture.meetspace.room.application.usecases.query.GetSiteQueryHandler;
import com.accenture.meetspace.room.application.usecases.query.ListRoomsQuery;
import com.accenture.meetspace.room.application.usecases.query.ListRoomsQueryHandler;
import com.accenture.meetspace.room.application.usecases.query.ListSitesQuery;
import com.accenture.meetspace.room.application.usecases.query.ListSitesQueryHandler;
import com.accenture.meetspace.room.application.usecases.query.RoomView;
import com.accenture.meetspace.room.application.usecases.query.SiteView;
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
