package com.accenture.meetspace.room.application.usecases.query;

import com.accenture.meetspace.room.domain.enums.Amenity;
import com.accenture.meetspace.room.domain.enums.RoomStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RoomView(UUID id, String code, String name, int floor, int capacity, RoomStatus status,
                       List<Amenity> amenities, RoomSiteView site) {

    public record RoomSiteView(UUID id, String code, String name, String timeZone) {
    }

    static final String COLUMNS = """
            select r.id, r.code, r.name, r.floor, r.capacity, r.status,
                   s.id as site_id, s.code as site_code, s.name as site_name, s.time_zone as site_time_zone
            """;

    static final String FROM = """
              from meeting_rooms r
              join sites s on s.id = r.site_id
            """;

    private static final String AMENITIES = """
            select room_id, amenity
              from meeting_room_amenities
             where room_id in (:roomIds)
             order by amenity
            """;

    static RoomView fromRow(ResultSet row, int rowNumber) throws SQLException {
        return new RoomView(
                UUID.fromString(row.getString("id")),
                row.getString("code"),
                row.getString("name"),
                row.getInt("floor"),
                row.getInt("capacity"),
                RoomStatus.valueOf(row.getString("status")),
                List.of(),
                new RoomSiteView(
                        UUID.fromString(row.getString("site_id")),
                        row.getString("site_code"),
                        row.getString("site_name"),
                        row.getString("site_time_zone")));
    }

    static List<RoomView> withAmenities(NamedParameterJdbcTemplate jdbc, List<RoomView> rooms) {
        if (rooms.isEmpty()) {
            return rooms;
        }
        var amenities = new HashMap<UUID, List<Amenity>>();
        jdbc.query(AMENITIES,
                Map.of("roomIds", rooms.stream().map(room -> room.id().toString()).toList()),
                row -> {
                    amenities.computeIfAbsent(UUID.fromString(row.getString("room_id")), id -> new ArrayList<>())
                            .add(Amenity.valueOf(row.getString("amenity")));
                });
        return rooms.stream()
                .map(room -> new RoomView(room.id(), room.code(), room.name(), room.floor(), room.capacity(),
                        room.status(), List.copyOf(amenities.getOrDefault(room.id(), List.of())), room.site()))
                .toList();
    }
}
