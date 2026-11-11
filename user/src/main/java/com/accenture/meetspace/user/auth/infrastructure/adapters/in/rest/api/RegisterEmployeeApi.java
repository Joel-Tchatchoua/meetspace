package com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeResponse;
import com.accenture.meetspace.user.auth.infrastructure.adapters.in.rest.requests.RegisterEmployeeRequest;
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
public interface RegisterEmployeeApi {

    @Operation(summary = "Register", description = "Creates an account with the EMPLOYEE role.")
    @ApiResponse(responseCode = "201", description = "Account created")
    @ApiResponse(responseCode = "400", description = "Invalid fields, weak password or different confirmation")
    @ApiResponse(responseCode = "409", description = "USERNAME_ALREADY_TAKEN or EMAIL_ALREADY_TAKEN")
    @ApiResponse(responseCode = "503", description = "Keycloak is unavailable")
    @PostMapping(path = "/register", version = "v1", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RegisterEmployeeResponse> register(@Valid @RequestBody RegisterEmployeeRequest request);
}
