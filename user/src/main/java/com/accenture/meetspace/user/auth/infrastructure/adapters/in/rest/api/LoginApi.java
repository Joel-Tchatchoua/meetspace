package com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.user.auth.application.usecases.command.login.LoginResponse;
import com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.requests.LoginRequest;
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
public interface LoginApi {

    @Operation(summary = "Log in", description = "Exchanges a username and password for tokens from Keycloak.")
    @ApiResponse(responseCode = "200", description = "Tokens")
    @ApiResponse(responseCode = "400", description = "Invalid fields")
    @ApiResponse(responseCode = "401", description = "INVALID_CREDENTIALS")
    @ApiResponse(responseCode = "403", description = "ACCOUNT_DISABLED")
    @ApiResponse(responseCode = "503", description = "Keycloak is unavailable")
    @PostMapping(path = "/login", version = "v1", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request);
}
