package com.accenture.meetspace.room.application.usecases.query.room;

import com.accenture.meetspace.room.domain.enums.Amenity;
import com.accenture.meetspace.room.domain.enums.RoomStatus;
import com.accenture.meetspace.room.domain.vo.Capacity;
import com.accenture.meetspace.bootstrap.application.query.InvalidQueryException;
import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.PaginatedQuery;
import com.accenture.meetspace.bootstrap.application.query.Pagination;
import com.accenture.meetspace.bootstrap.application.query.SortOrder;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record ListRoomsQuery(UUID siteId, Integer minCapacity, Set<Amenity> amenities, Integer floor,
                             RoomStatus status, Pagination pagination)
        implements PaginatedQuery<PageResponse<RoomView>> {

    private static final Map<String, String> SORTABLE_COLUMNS = Map.of(
            "code", "r.code",
            "name", "r.name",
            "floor", "r.floor",
            "capacity", "r.capacity",
            "status", "r.status");

    private static final SortOrder DEFAULT_SORT = new SortOrder("code", true);

    public ListRoomsQuery {
        if (siteId == null) {
            throw new InvalidQueryException("siteId", "must not be null");
        }
        if (minCapacity != null && minCapacity < Capacity.MIN) {
            throw new InvalidQueryException("minCapacity", "must be " + Capacity.MIN + " or greater, got "
                    + minCapacity);
        }
        amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
        pagination = pagination == null ? Pagination.DEFAULT : pagination;
    }

    @Override
    public Map<String, String> sortableColumns() {
        return SORTABLE_COLUMNS;
    }

    @Override
    public SortOrder defaultSort() {
        return DEFAULT_SORT;
    }
}
