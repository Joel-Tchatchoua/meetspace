package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.bootstrap.application.command.CommandBus;
import com.accenture.meetspace.user.profile.application.usecases.command.updateprofile.UpdateProfileResponse;
import com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.api.UpdateProfileApi;
import com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.requests.UpdateProfileRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class UpdateProfileAction implements UpdateProfileApi {

    private final CommandBus commandBus;

    public UpdateProfileAction(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Override
    public ResponseEntity<UpdateProfileResponse> updateProfile(UUID userId, UpdateProfileRequest request) {
        return ResponseEntity.ok(commandBus.dispatch(request.toCommand(userId)));
    }
}
