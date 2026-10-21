package com.accenture.meetspace.room.infrastructure.adapters.in.rest.api;

import com.accenture.meetspace.room.application.usecases.query.RoomView;
import com.accenture.meetspace.room.domain.enums.Amenity;
import com.accenture.meetspace.room.domain.enums.RoomStatus;
import com.accenture.meetspace.shared.application.query.PageResponse;
import com.accenture.meetspace.shared.application.query.Pagination;
import com.accenture.meetspace.shared.infrastructure.web.pagination.PaginationParameters;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;
import java.util.UUID;

@Tag(name = "Rooms", description = "Meeting rooms of a site")
@RequestMapping("/api/{version}/sites/{siteId}/rooms")
public interface ListRoomsApi {

    @Operation(
            summary = "List the rooms of a site",
            description = "Returns the rooms of a site that have not been removed, one page at a time. "
                    + "Filters can be combined. Sortable by code (default), name, floor, capacity and status.")
    @ApiResponse(
            responseCode = "200",
            description = "A page of rooms",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PageResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Invalid filter, page, size or sort (code INVALID_REQUEST)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Unknown or deleted site (code SITE_NOT_FOUND)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PaginationParameters
    @GetMapping(version = "v1", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageResponse<RoomView>> listRooms(
            @Parameter(description = "Id of the site", example = "3f2c8a8e-5b1d-4c47-9a5e-2d8f1b6c7e90")
            @PathVariable UUID siteId,
            @Parameter(description = "Minimum capacity, from 1", example = "6")
            @RequestParam(required = false) Integer minCapacity,
            @Parameter(description = "Amenities the room must all have; repeat the parameter for several")
            @RequestParam(required = false) Set<Amenity> amenities,
            @Parameter(description = "Floor of the room", example = "1")
            @RequestParam(required = false) Integer floor,
            @Parameter(description = "Status of the room")
            @RequestParam(required = false) RoomStatus status,
            @Parameter(hidden = true) Pagination pagination);
}
