package com.accenture.meetspace.room.infrastructure.persistence.models;

import com.accenture.meetspace.room.domain.enums.Amenity;
import com.accenture.meetspace.room.domain.enums.RoomStatus;
import com.accenture.meetspace.shared.infrastructure.persistence.VersionModel;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "meeting_rooms")
@Getter
@Setter
@NoArgsConstructor
public class MeetingRoomModel extends VersionModel {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteModel site;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int floor;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @ElementCollection
    @CollectionTable(name = "meeting_room_amenities", joinColumns = @JoinColumn(name = "room_id"))
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "amenity", nullable = false, length = 30)
    private Set<Amenity> amenities = new HashSet<>();
}
