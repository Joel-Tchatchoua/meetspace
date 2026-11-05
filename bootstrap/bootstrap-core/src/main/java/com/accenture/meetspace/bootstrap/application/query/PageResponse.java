package com.accenture.meetspace.bootstrap.application.query;

import java.util.List;

public record PageResponse<T>(List<T> content, PageMetadata page) {

    public record PageMetadata(int size, int number, long totalElements, int totalPages) {
    }

    public static <T> PageResponse<T> of(List<T> content, Pagination pagination, long totalElements) {
        var size = pagination.size();
        var totalPages = (int) ((totalElements + size - 1) / size);
        return new PageResponse<>(List.copyOf(content),
                new PageMetadata(size, pagination.page(), totalElements, totalPages));
    }
}
