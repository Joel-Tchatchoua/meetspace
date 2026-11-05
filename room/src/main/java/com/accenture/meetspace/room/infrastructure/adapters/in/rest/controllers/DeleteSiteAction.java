package com.accenture.meetspace.room.infrastructure.adapters.in.rest.controllers;

import com.accenture.meetspace.room.application.usecases.command.delete.DeleteSiteCommand;
import com.accenture.meetspace.room.application.usecases.command.delete.DeleteSiteResponse;
import com.accenture.meetspace.room.domain.vo.SiteId;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.api.DeleteSiteApi;
import com.accenture.meetspace.bootstrap.application.command.CommandBus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class DeleteSiteAction implements DeleteSiteApi {

    private final CommandBus commandBus;

    public DeleteSiteAction(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Override
    public ResponseEntity<DeleteSiteResponse> deleteSite(UUID id) {
        return ResponseEntity.ok(commandBus.dispatch(new DeleteSiteCommand(SiteId.of(id))));
    }
}
