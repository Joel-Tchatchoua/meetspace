package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.bootstrap.application.command.CommandBus;
import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusResponse;
import com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.api.ChangeUserStatusApi;
import com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.requests.ChangeUserStatusRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ChangeUserStatusAction implements ChangeUserStatusApi {

    private final CommandBus commandBus;

    public ChangeUserStatusAction(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Override
    public ResponseEntity<ChangeUserStatusResponse> changeStatus(UUID actorId, UUID id,
                                                                 ChangeUserStatusRequest request) {
        return ResponseEntity.ok(commandBus.dispatch(request.toCommand(actorId, id)));
    }
}
