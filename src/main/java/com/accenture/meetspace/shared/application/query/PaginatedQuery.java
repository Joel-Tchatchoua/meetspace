package com.accenture.meetspace.shared.application.query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface PaginatedQuery<R> extends Query<R> {

    Pagination pagination();

    Map<String, String> sortableColumns();

    SortOrder defaultSort();

    default String orderBy(String tieBreakerColumn) {
        var orders = pagination().sort().isEmpty() ? List.of(defaultSort()) : pagination().sort();
        return orders.stream()
                .map(order -> sortableColumns().get(order.property()) + (order.ascending() ? " asc" : " desc"))
                .collect(Collectors.joining(", ", "", ", " + tieBreakerColumn + " asc"));
    }
}
