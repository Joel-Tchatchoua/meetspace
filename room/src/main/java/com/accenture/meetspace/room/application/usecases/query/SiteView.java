package com.accenture.meetspace.room.application.usecases.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public record SiteView(UUID id, String code, String name, String address, String timeZone) {

    static final String COLUMNS = "id, code, name, address, time_zone";

    static SiteView fromRow(ResultSet row, int rowNumber) throws SQLException {
        return new SiteView(
                UUID.fromString(row.getString("id")),
                row.getString("code"),
                row.getString("name"),
                row.getString("address"),
                row.getString("time_zone"));
    }
}
