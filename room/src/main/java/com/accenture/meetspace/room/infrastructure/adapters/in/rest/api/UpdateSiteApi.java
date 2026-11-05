package com.accenture.meetspace.room.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.room.application.usecases.command.update.UpdateSiteResponse;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.requests.UpdateSiteRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Sites", description = "Office sites hosting meeting rooms")
@RequestMapping("/api/{version}/sites")
public interface UpdateSiteApi {

    @Operation(
            summary = "Update a site",
            description = "Replaces the code, name, address and time zone of a site. The new code and name must not "
                    + "be used by another site. The time zone defaults to Indian/Mauritius.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateSiteRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "EBX",
                                      "name": "Ebène Tower",
                                      "address": "Tower 1, Cybercity, Ebène",
                                      "timeZone": "Indian/Mauritius"
                                    }
                                    """))))
    @ApiResponse(
            responseCode = "200",
            description = "Site updated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UpdateSiteResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request: every invalid field is listed in errors, including an id that is not "
                    + "a UUID (code INVALID_REQUEST), or the body is not valid JSON (code MALFORMED_REQUEST)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Unknown or deleted site (code SITE_NOT_FOUND)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Code or name used by another site (code SITE_CODE_ALREADY_USED or SITE_NAME_ALREADY_USED), "
                    + "or the site was modified meanwhile (code CONCURRENT_UPDATE)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PutMapping(path = "/{id}", version = "v1", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UpdateSiteResponse> updateSite(
            @Parameter(description = "Id of the site", example = "3f2c8a8e-5b1d-4c47-9a5e-2d8f1b6c7e90")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSiteRequest request);
}
