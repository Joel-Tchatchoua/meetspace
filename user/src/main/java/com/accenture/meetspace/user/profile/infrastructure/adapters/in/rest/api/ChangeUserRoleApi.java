package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.user.profile.application.usecases.command.changerole.ChangeUserRoleResponse;
import com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.requests.ChangeUserRoleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Users", description = "Directory and administration of users")
@RequestMapping("/api/{version}/users")
public interface ChangeUserRoleApi {

    @Operation(summary = "Change the role of a user", description = "In both Keycloak and the profile.")
    @ApiResponse(responseCode = "200", description = "Role changed")
    @ApiResponse(responseCode = "400", description = "Invalid fields or unknown role")
    @ApiResponse(responseCode = "401", description = "UNAUTHENTICATED")
    @ApiResponse(responseCode = "403", description = "ACCESS_DENIED")
    @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND")
    @ApiResponse(responseCode = "409", description = "LAST_ACTIVE_ADMIN or CONCURRENT_UPDATE")
    @ApiResponse(responseCode = "503", description = "Keycloak is unavailable")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}/role", version = "v1", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ChangeUserRoleResponse> changeRole(@PathVariable UUID id,
                                                      @Valid @RequestBody ChangeUserRoleRequest request);
}
