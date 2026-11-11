package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.user.profile.application.usecases.query.user.UserView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Users", description = "Directory and administration of users")
@RequestMapping("/api/{version}/users")
public interface GetUserApi {

    @Operation(summary = "Get one user")
    @ApiResponse(responseCode = "200", description = "The user")
    @ApiResponse(responseCode = "401", description = "UNAUTHENTICATED")
    @ApiResponse(responseCode = "403", description = "ACCESS_DENIED")
    @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{id}", version = "v1", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserView> getUser(@PathVariable UUID id);
}
