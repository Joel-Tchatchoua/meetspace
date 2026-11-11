package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.bootstrap.application.command.CommandBus;
import com.accenture.meetspace.user.profile.application.usecases.command.changerole.ChangeUserRoleResponse;
import com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.api.ChangeUserRoleApi;
import com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.requests.ChangeUserRoleRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ChangeUserRoleAction implements ChangeUserRoleApi {

    private final CommandBus commandBus;

    public ChangeUserRoleAction(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Override
    public ResponseEntity<ChangeUserRoleResponse> changeRole(UUID id, ChangeUserRoleRequest request) {
        return ResponseEntity.ok(commandBus.dispatch(request.toCommand(id)));
    }
}
