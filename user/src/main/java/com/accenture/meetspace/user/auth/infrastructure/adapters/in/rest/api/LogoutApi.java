package com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.user.auth.application.usecases.command.logout.LogoutResponse;
import com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.requests.LogoutRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Authentication", description = "Registration, login and logout")
@RequestMapping("/api/{version}/auth")
public interface LogoutApi {

    @Operation(summary = "Log out", description = "Revokes the refresh token.")
    @ApiResponse(responseCode = "200", description = "Token revoked")
    @ApiResponse(responseCode = "400", description = "Invalid fields")
    @ApiResponse(responseCode = "503", description = "Keycloak is unavailable")
    @PostMapping(path = "/logout", version = "v1", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<LogoutResponse> logout(@Valid @RequestBody LogoutRequest request);
}
