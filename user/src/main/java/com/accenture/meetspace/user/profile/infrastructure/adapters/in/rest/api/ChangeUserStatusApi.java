package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.bootstrap.infrastructure.web.security.AuthenticatedUser;
import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusResponse;
import com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.requests.ChangeUserStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Users", description = "Directory and administration of users")
@RequestMapping("/api/{version}/users")
public interface ChangeUserStatusApi {

    @Operation(summary = "Activate or deactivate a user",
            description = "Deactivating also disables the user in Keycloak and cancels their upcoming reservations.")
    @ApiResponse(responseCode = "200", description = "Status changed")
    @ApiResponse(responseCode = "400", description = "Invalid fields")
    @ApiResponse(responseCode = "401", description = "UNAUTHENTICATED")
    @ApiResponse(responseCode = "403", description = "ACCESS_DENIED")
    @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND")
    @ApiResponse(responseCode = "409", description = "CANNOT_DEACTIVATE_OWN_ACCOUNT or CONCURRENT_UPDATE")
    @ApiResponse(responseCode = "503", description = "Keycloak is unavailable")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(path = "/{id}/status", version = "v1", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ChangeUserStatusResponse> changeStatus(
            @Parameter(hidden = true) @RequestAttribute(name = AuthenticatedUser.ID_ATTRIBUTE, required = false)
            UUID actorId,
            @PathVariable UUID id,
            @Valid @RequestBody ChangeUserStatusRequest request);
}
