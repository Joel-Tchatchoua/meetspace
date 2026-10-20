package com.accenture.meetspace.room.infrastructure.web.controllers;

import com.accenture.meetspace.room.application.command.create.CreateSiteResponse;
import com.accenture.meetspace.room.infrastructure.web.api.CreateSiteApi;
import com.accenture.meetspace.room.infrastructure.web.requests.CreateSiteRequest;
import com.accenture.meetspace.shared.application.command.CommandBus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class CreateSiteAction implements CreateSiteApi {

    private final CommandBus commandBus;

    public CreateSiteAction(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Override
    public ResponseEntity<CreateSiteResponse> createSite(CreateSiteRequest request) {
        var response = commandBus.dispatch(request.toCommand());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.siteId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }
}
