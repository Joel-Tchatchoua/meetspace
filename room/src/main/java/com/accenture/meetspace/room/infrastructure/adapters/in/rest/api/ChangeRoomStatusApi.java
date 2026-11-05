package com.accenture.meetspace.room.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.room.application.usecases.command.changestatus.ChangeRoomStatusResponse;
import com.accenture.meetspace.room.infrastructure.adapters.in.rest.requests.ChangeRoomStatusRequest;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Rooms", description = "Meeting rooms of a site")
@RequestMapping("/api/{version}/rooms")
public interface ChangeRoomStatusApi {

    @Operation(
            summary = "Change the status of a room",
            description = "Moves a room to ACTIVE, MAINTENANCE or CLOSED. Every transition is allowed. "
                    + "Only ACTIVE rooms can be booked.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChangeRoomStatusRequest.class),
                            examples = @ExampleObject(value = """
                                    { "status": "MAINTENANCE" }
                                    """))))
    @ApiResponse(
            responseCode = "200",
            description = "Status changed",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ChangeRoomStatusResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Missing status or id that is not a UUID (code INVALID_REQUEST), "
                    + "or unknown status value (code MALFORMED_REQUEST)",
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
            description = "The site was modified meanwhile (code CONCURRENT_UPDATE)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PatchMapping(path = "/{id}/status", version = "v1", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ChangeRoomStatusResponse> changeRoomStatus(
            @Parameter(description = "Id of the room", example = "7b1e2c4d-9a3f-4e8b-b6d2-1c5f8a9e0d34")
            @PathVariable UUID id,
            @Valid @RequestBody ChangeRoomStatusRequest request);
}
