package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.bootstrap.infrastructure.web.security.AuthenticatedUser;
import com.accenture.meetspace.user.profile.application.usecases.query.user.UserView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Profile", description = "My own profile")
@RequestMapping("/api/{version}/profile")
public interface GetProfileApi {

    @Operation(summary = "Show my profile")
    @ApiResponse(responseCode = "200", description = "My profile")
    @ApiResponse(responseCode = "401", description = "UNAUTHENTICATED")
    @ApiResponse(responseCode = "403", description = "ACCOUNT_DISABLED")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(version = "v1", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserView> getProfile(
            @Parameter(hidden = true) @RequestAttribute(name = AuthenticatedUser.ID_ATTRIBUTE, required = false)
            UUID userId);
}
