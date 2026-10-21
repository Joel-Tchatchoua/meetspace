package com.accenture.meetspace.room.infrastructure.adapters.in.rest.requests;

import com.accenture.meetspace.room.application.usecases.command.update.UpdateRoomCommand;
import com.accenture.meetspace.room.domain.enums.Amenity;
import com.accenture.meetspace.room.domain.vo.Capacity;
import com.accenture.meetspace.room.domain.vo.RoomId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record UpdateRoomRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 100) String name,
        @NotNull Integer floor,
        @NotNull @Min(Capacity.MIN) @Max(Capacity.MAX) Integer capacity,
        Set<Amenity> amenities) {

    public UpdateRoomCommand toCommand(UUID roomId) {
        return new UpdateRoomCommand(RoomId.of(roomId), code, name, floor, new Capacity(capacity), amenities);
    }
}
