package com.accenture.meetspace.user.profile.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.Pagination;
import com.accenture.meetspace.bootstrap.infrastructure.web.pagination.PaginationParameters;
import com.accenture.meetspace.user.profile.application.usecases.query.directory.DirectoryEntry;
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
public interface SearchDirectoryApi {

    @Operation(summary = "Search active colleagues by name", description = "Returns only basic public fields.")
    @ApiResponse(responseCode = "200", description = "A page of colleagues")
    @ApiResponse(responseCode = "400", description = "Missing name, or invalid page, size or sort")
    @ApiResponse(responseCode = "401", description = "UNAUTHENTICATED")
    @PaginationParameters
    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/directory", version = "v1", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageResponse<DirectoryEntry>> searchDirectory(
            @RequestParam(required = false) String name,
            @Parameter(hidden = true) Pagination pagination);
}
