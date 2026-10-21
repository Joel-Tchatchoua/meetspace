package com.accenture.meetspace.shared.application.query;

import java.util.List;

public record Pagination(int page, int size, List<SortOrder> sort) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;
    public static final Pagination DEFAULT = new Pagination(DEFAULT_PAGE, DEFAULT_SIZE, List.of());

    public Pagination {
        if (page < 0) {
            throw new InvalidQueryException("page", "must be 0 or greater, got " + page);
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new InvalidQueryException("size", "must be between 1 and " + MAX_SIZE + ", got " + size);
        }
        sort = sort == null ? List.of() : List.copyOf(sort);
    }

    public long offset() {
        return (long) page * size;
    }
}
