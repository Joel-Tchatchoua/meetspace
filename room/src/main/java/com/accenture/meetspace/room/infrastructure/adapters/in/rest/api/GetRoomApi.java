package com.accenture.meetspace.room.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.room.application.usecases.query.room.RoomView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Rooms", description = "Meeting rooms of a site")
@RequestMapping("/api/{version}/rooms")
public interface GetRoomApi {

    @Operation(
            summary = "Get a room",
            description = "Returns a room that has not been removed, with its site and the site time zone.")
    @ApiResponse(
            responseCode = "200",
            description = "The room",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RoomView.class)))
    @ApiResponse(
            responseCode = "400",
            description = "The id is not a UUID (code INVALID_REQUEST)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Unknown or removed room (code ROOM_NOT_FOUND)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/{id}", version = "v1", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RoomView> getRoom(
            @Parameter(description = "Id of the room", example = "7b1e2c4d-9a3f-4e8b-b6d2-1c5f8a9e0d34")
            @PathVariable UUID id);
}
