package com.accenture.meetspace.room.application.usecases.query;

import com.accenture.meetspace.room.domain.exceptions.RoomNotFoundException;
import com.accenture.meetspace.room.domain.vo.RoomId;
import com.accenture.meetspace.shared.application.query.QueryHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

public class GetRoomQueryHandler implements QueryHandler<GetRoomQuery, RoomView> {

    private static final String ROOM = RoomView.COLUMNS + RoomView.FROM + """
             where r.id = :id
               and r.deleted_at is null
               and s.deleted_at is null
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public GetRoomQueryHandler(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public RoomView handle(GetRoomQuery query) {
        var rooms = jdbc.query(ROOM, Map.of("id", query.roomId().toString()), RoomView::fromRow);
        return RoomView.withAmenities(jdbc, rooms).stream()
                .findFirst()
                .orElseThrow(() -> new RoomNotFoundException(RoomId.of(query.roomId())));
    }
}
