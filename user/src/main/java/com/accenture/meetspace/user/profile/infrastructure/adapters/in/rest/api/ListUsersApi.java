package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.Pagination;
import com.accenture.meetspace.bootstrap.infrastructure.web.pagination.PaginationParameters;
import com.accenture.meetspace.user.profile.application.usecases.query.user.UserView;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Users", description = "Directory and administration of users")
@RequestMapping("/api/{version}/users")
public interface ListUsersApi {

    @Operation(summary = "List all users", description = "Filters by username, email, department, role and status.")
    @ApiResponse(responseCode = "200", description = "A page of users")
    @ApiResponse(responseCode = "400", description = "Invalid filter, page, size or sort")
    @ApiResponse(responseCode = "401", description = "UNAUTHENTICATED")
    @ApiResponse(responseCode = "403", description = "ACCESS_DENIED")
    @PaginationParameters
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(version = "v1", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageResponse<UserView>> listUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) RoleEnum role,
            @RequestParam(required = false) Boolean active,
            @Parameter(hidden = true) Pagination pagination);
}
