package com.accenture.meetspace.room.application.usecases.query;

import com.accenture.meetspace.room.domain.exceptions.SiteNotFoundException;
import com.accenture.meetspace.room.domain.vo.SiteId;
import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.QueryHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

public class ListRoomsQueryHandler implements QueryHandler<ListRoomsQuery, PageResponse<RoomView>> {

    private static final String SITE_EXISTS = """
            select count(*)
              from sites
             where id = :siteId
               and deleted_at is null
            """;

    private static final String ROOMS_OF_SITE = """
             where r.site_id = :siteId
               and r.deleted_at is null
               and s.deleted_at is null
            """;

    private static final String HAS_EVERY_AMENITY = """
               and (select count(*)
                      from meeting_room_amenities a
                     where a.room_id = r.id
                       and a.amenity in (:amenities)) = :amenityCount
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public ListRoomsQueryHandler(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public PageResponse<RoomView> handle(ListRoomsQuery query) {
        var siteId = query.siteId().toString();
        if (count(SITE_EXISTS, new MapSqlParameterSource("siteId", siteId)) == 0) {
            throw new SiteNotFoundException(SiteId.of(query.siteId()));
        }
        var parameters = new MapSqlParameterSource("siteId", siteId);
        var where = new StringBuilder(RoomView.FROM).append(ROOMS_OF_SITE);
        if (query.minCapacity() != null) {
            where.append("   and r.capacity >= :minCapacity\n");
            parameters.addValue("minCapacity", query.minCapacity());
        }
        if (query.floor() != null) {
            where.append("   and r.floor = :floor\n");
            parameters.addValue("floor", query.floor());
        }
        if (query.status() != null) {
            where.append("   and r.status = :status\n");
            parameters.addValue("status", query.status().name());
        }
        if (!query.amenities().isEmpty()) {
            where.append(HAS_EVERY_AMENITY);
            parameters.addValue("amenities", query.amenities().stream().map(Enum::name).toList());
            parameters.addValue("amenityCount", query.amenities().size());
        }
        var pagination = query.pagination();
        var total = count("select count(*)" + where, parameters);
        parameters.addValues(Map.of("limit", pagination.size(), "offset", pagination.offset()));
        var rooms = jdbc.query(RoomView.COLUMNS + where + " order by " + query.orderBy("r.id")
                + " limit :limit offset :offset", parameters, RoomView::fromRow);
        return PageResponse.of(RoomView.withAmenities(jdbc, rooms), pagination, total);
    }

    private long count(String sql, MapSqlParameterSource parameters) {
        var count = jdbc.queryForObject(sql, parameters, Long.class);
        return count == null ? 0 : count;
    }
}
