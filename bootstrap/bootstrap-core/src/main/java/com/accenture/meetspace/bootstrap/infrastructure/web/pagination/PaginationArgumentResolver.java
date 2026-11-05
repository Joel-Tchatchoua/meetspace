package com.accenture.meetspace.bootstrap.infrastructure.web.pagination;

import com.accenture.meetspace.bootstrap.application.query.InvalidQueryException;
import com.accenture.meetspace.bootstrap.application.query.Pagination;
import com.accenture.meetspace.bootstrap.application.query.SortOrder;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.List;

public class PaginationArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Pagination.class.equals(parameter.getParameterType());
    }

    @Override
    public Pagination resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest request, WebDataBinderFactory binderFactory) {
        return new Pagination(
                integer(request.getParameter("page"), "page", Pagination.DEFAULT_PAGE),
                integer(request.getParameter("size"), "size", Pagination.DEFAULT_SIZE),
                sort(request.getParameterValues("sort")));
    }

    private static int integer(String value, String field, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new InvalidQueryException(field, "must be an integer, got " + value);
        }
    }

    private static List<SortOrder> sort(String[] values) {
        if (values == null) {
            return List.of();
        }
        return Arrays.stream(values)
                .filter(value -> !value.isBlank())
                .map(PaginationArgumentResolver::sortOrder)
                .toList();
    }

    private static SortOrder sortOrder(String value) {
        var parts = value.split(",", -1);
        if (parts.length > 2 || parts[0].isBlank()) {
            throw new InvalidQueryException("sort", "must be property or property,asc|desc, got " + value);
        }
        if (parts.length == 1 || parts[1].equalsIgnoreCase("asc")) {
            return new SortOrder(parts[0].trim(), true);
        }
        if (parts[1].equalsIgnoreCase("desc")) {
            return new SortOrder(parts[0].trim(), false);
        }
        throw new InvalidQueryException("sort", "direction must be asc or desc, got " + parts[1]);
    }
}
