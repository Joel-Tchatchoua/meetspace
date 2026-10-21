package com.accenture.meetspace.room.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.room.application.usecases.command.changestatus.ChangeRoomStatusResponse;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.api.ChangeRoomStatusApi;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.requests.ChangeRoomStatusRequest;
import com.accenture.meetspace.shared.application.command.CommandBus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ChangeRoomStatusAction implements ChangeRoomStatusApi {

    private final CommandBus commandBus;

    public ChangeRoomStatusAction(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Override
    public ResponseEntity<ChangeRoomStatusResponse> changeRoomStatus(UUID id, ChangeRoomStatusRequest request) {
        return ResponseEntity.ok(commandBus.dispatch(request.toCommand(id)));
    }
}
