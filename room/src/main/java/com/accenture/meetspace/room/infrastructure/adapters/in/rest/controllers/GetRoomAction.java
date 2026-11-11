package com.accenture.meetspace.room.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.room.application.usecases.query.room.GetRoomQuery;
import com.accenture.meetspace.room.application.usecases.query.room.RoomView;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.api.GetRoomApi;
import com.accenture.meetspace.bootstrap.application.query.QueryBus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class GetRoomAction implements GetRoomApi {

    private final QueryBus queryBus;

    public GetRoomAction(QueryBus queryBus) {
        this.queryBus = queryBus;
    }

    @Override
    public ResponseEntity<RoomView> getRoom(UUID id) {
        return ResponseEntity.ok(queryBus.ask(new GetRoomQuery(id)));
    }
}
