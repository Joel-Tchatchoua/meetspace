package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.bootstrap.infrastructure.web.security.AuthenticatedUser;
import com.accenture.meetspace.user.profile.application.usecases.command.updateprofile.UpdateProfileResponse;
import com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.requests.UpdateProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Profile", description = "My own profile")
@RequestMapping("/api/{version}/profile")
public interface UpdateProfileApi {

    @Operation(summary = "Update my profile", description = "Only the fields that are sent are changed.")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @ApiResponse(responseCode = "400", description = "Invalid fields")
    @ApiResponse(responseCode = "401", description = "UNAUTHENTICATED")
    @ApiResponse(responseCode = "403", description = "ACCOUNT_DISABLED")
    @ApiResponse(responseCode = "404", description = "DEFAULT_SITE_NOT_FOUND")
    @PreAuthorize("isAuthenticated()")
    @PatchMapping(version = "v1", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UpdateProfileResponse> updateProfile(
            @Parameter(hidden = true) @RequestAttribute(name = AuthenticatedUser.ID_ATTRIBUTE, required = false)
            UUID userId,
            @Valid @RequestBody UpdateProfileRequest request);
}
