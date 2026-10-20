package com.accenture.meetspace.room.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.room.application.usecases.create.CreateSiteResponse;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.requests.CreateSiteRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Sites", description = "Office sites hosting meeting rooms")
@RequestMapping("/api/v1/sites")
public interface CreateSiteApi {

    @Operation(
            summary = "Create a site",
            description = "Creates an office site. Its code and name must not be used by another site. "
                    + "The time zone defaults to Indian/Mauritius.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateSiteRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "EBN",
                                      "name": "Ebène",
                                      "address": "Cybercity, Ebène",
                                      "timeZone": "Indian/Mauritius"
                                    }
                                    """))))
    @ApiResponse(
            responseCode = "201",
            description = "Site created; its URL is in the Location header",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateSiteResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request: every invalid field is listed in errors (code INVALID_REQUEST), "
                    + "or the body is not valid JSON (code MALFORMED_REQUEST)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Code or name already used by another site "
                    + "(code SITE_CODE_ALREADY_USED or SITE_NAME_ALREADY_USED)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CreateSiteResponse> createSite(@Valid @RequestBody CreateSiteRequest request);
}
