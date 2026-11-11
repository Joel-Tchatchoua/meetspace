package com.accenture.meetspace.room.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.room.application.usecases.command.update.UpdateRoomResponse;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.requests.UpdateRoomRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Rooms", description = "Meeting rooms of a site")
@RequestMapping("/api/{version}/rooms")
public interface UpdateRoomApi {

    @Operation(
            summary = "Update a room",
            description = "Replaces the code, name, floor, capacity and amenities of a room. Its site and status "
                    + "do not change. The new code must not be used by another room of the site.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateRoomRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "R103",
                                      "name": "Dodo XL",
                                      "floor": 3,
                                      "capacity": 20,
                                      "amenities": ["VIDEO_CONFERENCE"]
                                    }
                                    """))))
    @ApiResponse(
            responseCode = "200",
            description = "Room updated",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UpdateRoomResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request: every invalid field is listed in errors, including an id that is not "
                    + "a UUID (code INVALID_REQUEST), or the body is not valid JSON or holds an unknown amenity "
                    + "(code MALFORMED_REQUEST)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Unknown or removed room (code ROOM_NOT_FOUND)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Code used by another room of the site (code ROOM_CODE_ALREADY_USED), "
                    + "or the site was modified meanwhile (code CONCURRENT_UPDATE)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}", version = "v1", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UpdateRoomResponse> updateRoom(
            @Parameter(description = "Id of the room", example = "7b1e2c4d-9a3f-4e8b-b6d2-1c5f8a9e0d34")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoomRequest request);
}
