package com.accenture.meetspace.room.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.room.application.usecases.command.delete.DeleteSiteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Sites", description = "Office sites hosting meeting rooms")
@RequestMapping("/api/{version}/sites")
public interface DeleteSiteApi {

    @Operation(
            summary = "Delete a site",
            description = "Soft-deletes a site: it stays in the database but can no longer be read or changed, "
                    + "and its code and name become free again. A site that still has rooms cannot be deleted.")
    @ApiResponse(
            responseCode = "200",
            description = "Site deleted",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DeleteSiteResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "The id is not a UUID (code INVALID_REQUEST)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Unknown or already deleted site (code SITE_NOT_FOUND)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "The site still has rooms (code SITE_HAS_ROOMS), "
                    + "or it was modified meanwhile (code CONCURRENT_UPDATE)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @DeleteMapping(path = "/{id}", version = "v1", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<DeleteSiteResponse> deleteSite(
            @Parameter(description = "Id of the site", example = "3f2c8a8e-5b1d-4c47-9a5e-2d8f1b6c7e90")
            @PathVariable UUID id);
}
