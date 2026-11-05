package com.accenture.meetspace.room.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.room.application.usecases.query.SiteView;
import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.Pagination;
import com.accenture.meetspace.bootstrap.infrastructure.web.pagination.PaginationParameters;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Sites", description = "Office sites hosting meeting rooms")
@RequestMapping("/api/{version}/sites")
public interface ListSitesApi {

    @Operation(
            summary = "List the sites",
            description = "Returns the sites that have not been deleted, one page at a time. "
                    + "Sortable by code (default), name, address and timeZone, e.g. sort=name,desc. "
                    + "The page size goes from 1 to 100 and defaults to 20.")
    @ApiResponse(
            responseCode = "200",
            description = "A page of sites",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PageResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Invalid page, size or sort (code INVALID_REQUEST)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PaginationParameters
    @GetMapping(version = "v1", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageResponse<SiteView>> listSites(@Parameter(hidden = true) Pagination pagination);
}
