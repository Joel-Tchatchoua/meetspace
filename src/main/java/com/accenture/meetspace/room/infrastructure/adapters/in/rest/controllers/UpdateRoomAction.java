package com.accenture.meetspace.room.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.room.application.usecases.command.update.UpdateRoomResponse;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.api.UpdateRoomApi;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.requests.UpdateRoomRequest;
import com.accenture.meetspace.shared.application.command.CommandBus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class UpdateRoomAction implements UpdateRoomApi {

    private final CommandBus commandBus;

    public UpdateRoomAction(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Override
    public ResponseEntity<UpdateRoomResponse> updateRoom(UUID id, UpdateRoomRequest request) {
        return ResponseEntity.ok(commandBus.dispatch(request.toCommand(id)));
    }
}
