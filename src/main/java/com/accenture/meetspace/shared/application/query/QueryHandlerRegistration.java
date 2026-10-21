package com.accenture.meetspace.shared.application.query;

import java.util.Objects;

public record QueryHandlerRegistration<Q extends Query<R>, R>(Class<Q> queryType, QueryHandler<Q, R> handler) {

    public QueryHandlerRegistration {
        Objects.requireNonNull(queryType, "queryType");
        Objects.requireNonNull(handler, "handler");
    }

    public R handle(Query<?> query) {
        return handler.handle(queryType.cast(query));
    }
}
