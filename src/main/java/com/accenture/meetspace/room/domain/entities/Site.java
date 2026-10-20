package com.accenture.meetspace.room.domain.entities;

import com.accenture.meetspace.room.domain.enums.Amenity;
import com.accenture.meetspace.room.domain.exceptions.RoomCodeAlreadyUsedException;
import com.accenture.meetspace.room.domain.exceptions.RoomNotFoundException;
import com.accenture.meetspace.room.domain.exceptions.SiteHasRoomsException;
import com.accenture.meetspace.room.domain.vo.Capacity;
import com.accenture.meetspace.room.domain.vo.RoomId;
import com.accenture.meetspace.room.domain.vo.SiteId;

import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class Site {

    public static final String DEFAULT_TIME_ZONE = "Indian/Mauritius";

    public record Snapshot(SiteId id, String code, String name, String address, ZoneId timeZone, boolean deleted,
                           List<MeetingRoom.Snapshot> rooms) {

        public Optional<MeetingRoom.Snapshot> room(RoomId roomId) {
            return rooms.stream().filter(room -> room.id().equals(roomId)).findFirst();
        }
    }

    private final SiteId id;
    private String code;
    private String name;
    private String address;
    private ZoneId timeZone;
    private boolean deleted;
    private final Map<RoomId, MeetingRoom> rooms = new LinkedHashMap<>();

    private Site(SiteId id, String code, String name, String address, ZoneId timeZone) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.address = address;
        this.timeZone = timeZone;
    }

    public static Site create(SiteId id, String code, String name, String address, ZoneId timeZone) {
        return new Site(id, code, name, address, timeZone);
    }

    public static Site restore(Snapshot snapshot) {
        var site = new Site(snapshot.id(), snapshot.code(), snapshot.name(), snapshot.address(), snapshot.timeZone());
        site.deleted = snapshot.deleted();
        snapshot.rooms().forEach(room -> site.rooms.put(room.id(), MeetingRoom.restore(room)));
        return site;
    }

    public void changeDetails(String code, String name, String address, ZoneId timeZone) {
        this.code = code;
        this.name = name;
        this.address = address;
        this.timeZone = timeZone;
    }

    public void delete() {
        if (hasRooms()) {
            throw new SiteHasRoomsException(id);
        }
        deleted = true;
    }

    public void addRoom(RoomId roomId, String code, String name, int floor, Capacity capacity,
                        Set<Amenity> amenities) {
        ensureRoomCodeIsFree(code);
        rooms.put(roomId, MeetingRoom.create(roomId, code, name, floor, capacity, amenities));
    }

    public void changeRoomDetails(RoomId roomId, String code, String name, int floor, Capacity capacity,
                                  Set<Amenity> amenities) {
        var room = activeRoom(roomId);
        if (!room.hasCode(code)) {
            ensureRoomCodeIsFree(code);
        }
        room.changeDetails(code, name, floor, capacity, amenities);
    }

    public void activateRoom(RoomId roomId) {
        activeRoom(roomId).activate();
    }

    public void putRoomUnderMaintenance(RoomId roomId) {
        activeRoom(roomId).putUnderMaintenance();
    }

    public void closeRoom(RoomId roomId) {
        activeRoom(roomId).close();
    }

    public void removeRoom(RoomId roomId) {
        activeRoom(roomId).delete();
    }

    public Snapshot snapshot() {
        return new Snapshot(id, code, name, address, timeZone, deleted,
                rooms.values().stream().map(MeetingRoom::snapshot).toList());
    }

    private boolean hasRooms() {
        return rooms.values().stream().anyMatch(room -> !room.isDeleted());
    }

    private MeetingRoom activeRoom(RoomId roomId) {
        return Optional.ofNullable(rooms.get(roomId))
                .filter(room -> !room.isDeleted())
                .orElseThrow(() -> new RoomNotFoundException(roomId));
    }

    private void ensureRoomCodeIsFree(String code) {
        if (rooms.values().stream().anyMatch(room -> !room.isDeleted() && room.hasCode(code))) {
            throw new RoomCodeAlreadyUsedException(id, code);
        }
    }
}
