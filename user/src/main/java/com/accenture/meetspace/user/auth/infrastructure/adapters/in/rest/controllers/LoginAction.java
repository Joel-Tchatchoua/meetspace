package com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.bootstrap.application.command.CommandBus;
import com.accenture.meetspace.user.auth.application.usecases.command.login.LoginResponse;
import com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.api.LoginApi;
import com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.requests.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginAction implements LoginApi {

    private final CommandBus commandBus;

    public LoginAction(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        return ResponseEntity.ok(commandBus.dispatch(request.toCommand()));
    }
}
