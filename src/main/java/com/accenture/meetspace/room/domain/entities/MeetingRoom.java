package com.accenture.meetspace.room.domain.entities;

import com.accenture.meetspace.room.domain.enums.Amenity;
import com.accenture.meetspace.room.domain.enums.RoomStatus;
import com.accenture.meetspace.room.domain.vo.Capacity;
import com.accenture.meetspace.room.domain.vo.RoomId;

import java.util.Set;

public final class MeetingRoom {

    public record Snapshot(RoomId id, String code, String name, int floor, Capacity capacity,
                           Set<Amenity> amenities, RoomStatus status, boolean deleted) {
    }

    private final RoomId id;
    private String code;
    private String name;
    private int floor;
    private Capacity capacity;
    private Set<Amenity> amenities;
    private RoomStatus status;
    private boolean deleted;

    private MeetingRoom(RoomId id, String code, String name, int floor, Capacity capacity, Set<Amenity> amenities) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.floor = floor;
        this.capacity = capacity;
        this.amenities = Set.copyOf(amenities);
        this.status = RoomStatus.ACTIVE;
    }

    static MeetingRoom create(RoomId id, String code, String name, int floor, Capacity capacity,
                              Set<Amenity> amenities) {
        return new MeetingRoom(id, code, name, floor, capacity, amenities);
    }

    static MeetingRoom restore(Snapshot snapshot) {
        var room = new MeetingRoom(snapshot.id(), snapshot.code(), snapshot.name(), snapshot.floor(),
                snapshot.capacity(), snapshot.amenities());
        room.status = snapshot.status();
        room.deleted = snapshot.deleted();
        return room;
    }

    void changeDetails(String code, String name, int floor, Capacity capacity, Set<Amenity> amenities) {
        this.code = code;
        this.name = name;
        this.floor = floor;
        this.capacity = capacity;
        this.amenities = Set.copyOf(amenities);
    }

    void activate() {
        status = RoomStatus.ACTIVE;
    }

    void putUnderMaintenance() {
        status = RoomStatus.MAINTENANCE;
    }

    void close() {
        status = RoomStatus.CLOSED;
    }

    void delete() {
        deleted = true;
    }

    boolean hasCode(String code) {
        return this.code.equals(code);
    }

    boolean isDeleted() {
        return deleted;
    }

    public Snapshot snapshot() {
        return new Snapshot(id, code, name, floor, capacity, amenities, status, deleted);
    }
}
