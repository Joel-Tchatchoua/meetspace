package com.accenture.meetspace.room.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.room.application.usecases.command.delete.DeleteRoomCommand;
import com.accenture.meetspace.room.application.usecases.command.delete.DeleteRoomResponse;
import com.accenture.meetspace.room.domain.vo.RoomId;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.api.DeleteRoomApi;
import com.accenture.meetspace.bootstrap.application.command.CommandBus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class DeleteRoomAction implements DeleteRoomApi {

    private final CommandBus commandBus;

    public DeleteRoomAction(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Override
    public ResponseEntity<DeleteRoomResponse> deleteRoom(UUID id) {
        return ResponseEntity.ok(commandBus.dispatch(new DeleteRoomCommand(RoomId.of(id))));
    }
}
