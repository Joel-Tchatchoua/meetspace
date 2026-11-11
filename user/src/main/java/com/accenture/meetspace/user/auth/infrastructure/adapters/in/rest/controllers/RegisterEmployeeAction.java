package com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.bootstrap.application.command.CommandBus;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeResponse;
import com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.api.RegisterEmployeeApi;
import com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.requests.RegisterEmployeeRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RegisterEmployeeAction implements RegisterEmployeeApi {

    private final CommandBus commandBus;

    public RegisterEmployeeAction(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Override
    public ResponseEntity<RegisterEmployeeResponse> register(RegisterEmployeeRequest request) {
        var response = commandBus.dispatch(request.toCommand());
        return ResponseEntity.created(URI.create("/api/v1/users/" + response.userId())).body(response);
    }
}
