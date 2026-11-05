package com.accenture.meetspace.room.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.room.application.usecases.query.ListRoomsQuery;
import com.accenture.meetspace.room.application.usecases.query.RoomView;
import com.accenture.meetspace.room.domain.enums.Amenity;
import com.accenture.meetspace.room.domain.enums.RoomStatus;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.api.ListRoomsApi;
import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.Pagination;
import com.accenture.meetspace.bootstrap.application.query.QueryBus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
public class ListRoomsAction implements ListRoomsApi {

    private final QueryBus queryBus;

    public ListRoomsAction(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @Override
    public ResponseEntity<PageResponse<RoomView>> listRooms(UUID siteId, Integer minCapacity, Set<Amenity> amenities,
                                                            Integer floor, RoomStatus status, Pagination pagination) {
        return ResponseEntity.ok(queryBus.ask(
                new ListRoomsQuery(siteId, minCapacity, amenities, floor, status, pagination)));
    }
}
