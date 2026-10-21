package com.accenture.meetspace.room.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.room.application.usecases.update.UpdateSiteResponse;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.api.UpdateSiteApi;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.requests.UpdateSiteRequest;
import com.accenture.meetspace.shared.application.command.CommandBus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class UpdateSiteAction implements UpdateSiteApi {

    private final CommandBus commandBus;

    public UpdateSiteAction(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Override
    public ResponseEntity<UpdateSiteResponse> updateSite(UUID id, UpdateSiteRequest request) {
        return ResponseEntity.ok(commandBus.dispatch(request.toCommand(id)));
    }
}
