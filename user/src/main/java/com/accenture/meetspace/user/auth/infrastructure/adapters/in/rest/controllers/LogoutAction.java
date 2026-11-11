package com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.bootstrap.application.command.CommandBus;
import com.accenture.meetspace.user.auth.application.usecases.command.logout.LogoutResponse;
import com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.api.LogoutApi;
import com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.requests.LogoutRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutAction implements LogoutApi {

    private final CommandBus commandBus;

    public LogoutAction(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Override
    public ResponseEntity<LogoutResponse> logout(LogoutRequest request) {
        return ResponseEntity.ok(commandBus.dispatch(request.toCommand()));
    }
}
