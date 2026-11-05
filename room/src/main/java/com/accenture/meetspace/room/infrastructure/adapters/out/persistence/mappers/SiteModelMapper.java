package com.accenture.meetspace.room.infrastructure.adapters.out.persistence.mappers;

import com.accenture.meetspace.room.domain.entities.MeetingRoom;
import com.accenture.meetspace.room.domain.entities.Site;
import com.accenture.meetspace.room.domain.vo.Capacity;
import com.accenture.meetspace.room.domain.vo.RoomId;
import com.accenture.meetspace.room.domain.vo.SiteId;
import com.accenture.meetspace.room.infrastructure.adapters.out.persistence.models.MeetingRoomModel;
import com.accenture.meetspace.room.infrastructure.adapters.out.persistence.models.SiteModel;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SiteModelMapper {

    private final Clock clock;

    public SiteModelMapper(Clock clock) {
        this.clock = clock;
    }

    public Site toDomain(SiteModel model) {
        return Site.restore(new Site.Snapshot(
                SiteId.of(UUID.fromString(model.getId())),
                model.getCode(),
                model.getName(),
                model.getAddress(),
                ZoneId.of(model.getTimeZone()),
                model.getDeletedAt() != null,
                model.getRooms().stream().map(this::toSnapshot).toList()));
    }

    public SiteModel toNewModel(Site.Snapshot snapshot) {
        var model = new SiteModel();
        model.setId(snapshot.id().value().toString());
        update(model, snapshot);
        return model;
    }

    public void update(SiteModel model, Site.Snapshot snapshot) {
        model.setCode(snapshot.code());
        model.setName(snapshot.name());
        model.setAddress(snapshot.address());
        model.setTimeZone(snapshot.timeZone().getId());
        model.setDeletedAt(deletedAt(model.getDeletedAt(), snapshot.deleted()));
        Map<String, MeetingRoomModel> existingRooms = model.getRooms().stream()
                .collect(Collectors.toMap(MeetingRoomModel::getId, Function.identity()));
        snapshot.rooms().forEach(room -> {
            var roomModel = existingRooms.get(room.id().value().toString());
            if (roomModel == null) {
                roomModel = new MeetingRoomModel();
                roomModel.setId(room.id().value().toString());
                roomModel.setSite(model);
                model.getRooms().add(roomModel);
            }
            update(roomModel, room);
        });
    }

    private MeetingRoom.Snapshot toSnapshot(MeetingRoomModel model) {
        return new MeetingRoom.Snapshot(
                RoomId.of(UUID.fromString(model.getId())),
                model.getCode(),
                model.getName(),
                model.getFloor(),
                new Capacity(model.getCapacity()),
                Set.copyOf(model.getAmenities()),
                model.getStatus(),
                model.getDeletedAt() != null);
    }

    private void update(MeetingRoomModel model, MeetingRoom.Snapshot snapshot) {
        model.setCode(snapshot.code());
        model.setName(snapshot.name());
        model.setFloor(snapshot.floor());
        model.setCapacity(snapshot.capacity().value());
        model.setStatus(snapshot.status());
        model.setAmenities(new HashSet<>(snapshot.amenities()));
        model.setDeletedAt(deletedAt(model.getDeletedAt(), snapshot.deleted()));
    }

    private Instant deletedAt(Instant current, boolean deleted) {
        if (!deleted) {
            return null;
        }
        return current != null ? current : clock.instant();
    }
}
