package com.accenture.meetspace.room.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.room.application.usecases.command.delete.DeleteRoomResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Rooms", description = "Meeting rooms of a site")
@RequestMapping("/api/{version}/rooms")
public interface DeleteRoomApi {

    @Operation(
            summary = "Remove a room",
            description = "Soft-deletes a room: it stays in the database but can no longer be read or changed, "
                    + "and its code becomes free again in its site.")
    @ApiResponse(
            responseCode = "200",
            description = "Room removed",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DeleteRoomResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "The id is not a UUID (code INVALID_REQUEST)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Unknown or already removed room (code ROOM_NOT_FOUND)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "The site was modified meanwhile (code CONCURRENT_UPDATE)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}", version = "v1", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<DeleteRoomResponse> deleteRoom(
            @Parameter(description = "Id of the room", example = "7b1e2c4d-9a3f-4e8b-b6d2-1c5f8a9e0d34")
            @PathVariable UUID id);
}
