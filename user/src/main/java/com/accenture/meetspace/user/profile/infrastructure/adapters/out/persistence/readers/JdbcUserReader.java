package com.accenture.meetspace.user.profile.infrastructure.adapters.out.persistence.readers;

import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.Pagination;
import com.accenture.meetspace.bootstrap.application.query.PaginatedQuery;
import com.accenture.meetspace.user.profile.application.port.out.reader.UserReader;
import com.accenture.meetspace.user.profile.application.usecases.query.directory.DirectoryEntry;
import com.accenture.meetspace.user.profile.application.usecases.query.user.ListUsersQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.directory.SearchDirectoryQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.user.UserView;
import com.accenture.meetspace.user.profile.domain.enums.RoleEnum;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcUserReader implements UserReader {

    private static final String COLUMNS = """
            select u.id, u.username, u.email, u.first_name, u.last_name, u.department, u.phone,
                   u.default_site_id, u.role, u.active
            """;

    private static final String FROM_USERS = """
              from users u
             where u.deleted_at is null
            """;

    private static final String DIRECTORY_COLUMNS = "select u.id, u.username, u.first_name, u.last_name\n";

    private static final String MATCHES_NAME = """
               and u.active = true
               and (u.first_name like concat('%', :name, '%') or u.last_name like concat('%', :name, '%'))
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcUserReader(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<UserView> findById(UUID id) {
        return jdbc.query(COLUMNS + FROM_USERS + "   and u.id = :id\n",
                        new MapSqlParameterSource("id", id.toString()), JdbcUserReader::toView)
                .stream()
                .findFirst();
    }

    @Override
    public PageResponse<UserView> search(ListUsersQuery query) {
        var parameters = new MapSqlParameterSource();
        var where = new StringBuilder(FROM_USERS);
        contains(where, parameters, "username", query.username());
        contains(where, parameters, "email", query.email());
        contains(where, parameters, "department", query.department());
        if (query.role() != null) {
            where.append("   and u.role = :role\n");
            parameters.addValue("role", query.role().name());
        }
        if (query.active() != null) {
            where.append("   and u.active = :active\n");
            parameters.addValue("active", query.active());
        }
        return page(query, COLUMNS, where.toString(), parameters, JdbcUserReader::toView);
    }

    @Override
    public PageResponse<DirectoryEntry> searchDirectory(SearchDirectoryQuery query) {
        return page(query, DIRECTORY_COLUMNS, FROM_USERS + MATCHES_NAME,
                new MapSqlParameterSource("name", query.name()), (row, rowNumber) -> new DirectoryEntry(
                        UUID.fromString(row.getString("id")), row.getString("username"),
                        row.getString("first_name"), row.getString("last_name")));
    }

    private static void contains(StringBuilder where, MapSqlParameterSource parameters, String column, String value) {
        if (value != null) {
            where.append("   and u.").append(column).append(" like concat('%', :").append(column).append(", '%')\n");
            parameters.addValue(column, value);
        }
    }

    private <T> PageResponse<T> page(PaginatedQuery<?> query, String columns, String where,
                                     MapSqlParameterSource parameters, RowMapper<T> mapper) {
        Pagination pagination = query.pagination();
        var total = jdbc.queryForObject("select count(*)\n" + where, parameters, Long.class);
        parameters.addValues(Map.of("limit", pagination.size(), "offset", pagination.offset()));
        var content = jdbc.query(columns + where + " order by " + query.orderBy("u.id")
                + " limit :limit offset :offset", parameters, mapper);
        return PageResponse.of(content, pagination, total == null ? 0 : total);
    }

    private static UserView toView(ResultSet row, int rowNumber) throws SQLException {
        var defaultSiteId = row.getString("default_site_id");
        return new UserView(
                UUID.fromString(row.getString("id")),
                row.getString("username"),
                row.getString("email"),
                row.getString("first_name"),
                row.getString("last_name"),
                row.getString("department"),
                row.getString("phone"),
                defaultSiteId == null ? null : UUID.fromString(defaultSiteId),
                RoleEnum.valueOf(row.getString("role")),
                row.getBoolean("active"));
    }
}
