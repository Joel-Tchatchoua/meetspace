package com.accenture.meetspace.room.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.room.application.usecases.command.create.CreateRoomResponse;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.api.CreateRoomApi;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.requests.CreateRoomRequest;
import com.accenture.meetspace.bootstrap.application.command.CommandBus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
public class  CreateRoomAction implements CreateRoomApi {

    private static final String ROOM_PATH = "/api/v1/rooms/{id}";

    private final CommandBus commandBus;

    public CreateRoomAction(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Override
    public ResponseEntity<CreateRoomResponse> createRoom(UUID siteId, CreateRoomRequest request) {
        var response = commandBus.dispatch(request.toCommand(siteId));
        var location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(ROOM_PATH)
                .buildAndExpand(response.roomId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }
}
