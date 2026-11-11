package com.accenture.meetspace.room.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.room.application.usecases.command.create.CreateRoomResponse;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.requests.CreateRoomRequest;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Rooms", description = "Meeting rooms of a site")
@RequestMapping("/api/{version}/sites/{siteId}/rooms")
public interface CreateRoomApi {

    @Operation(
            summary = "Create a room in a site",
            description = "Adds an ACTIVE room to a site. Its code must not be used by another room of the site. "
                    + "The capacity goes from 1 to 200; the floor may be negative; amenities are optional.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateRoomRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "R101",
                                      "name": "Dodo",
                                      "floor": 1,
                                      "capacity": 8,
                                      "amenities": ["PROJECTOR", "WHITEBOARD"]
                                    }
                                    """))))
    @ApiResponse(
            responseCode = "201",
            description = "Room created; its URL is in the Location header",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateRoomResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request: every invalid field is listed in errors (code INVALID_REQUEST), "
                    + "or the body is not valid JSON or holds an unknown amenity (code MALFORMED_REQUEST)",
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
            description = "Code already used by another room of the site (code ROOM_CODE_ALREADY_USED), "
                    + "or the site was modified meanwhile (code CONCURRENT_UPDATE)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(version = "v1", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CreateRoomResponse> createRoom(
            @Parameter(description = "Id of the site", example = "3f2c8a8e-5b1d-4c47-9a5e-2d8f1b6c7e90")
            @PathVariable UUID siteId,
            @Valid @RequestBody CreateRoomRequest request);
}
