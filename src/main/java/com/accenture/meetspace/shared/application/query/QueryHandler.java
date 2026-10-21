package com.accenture.meetspace.shared.application.query;

@FunctionalInterface
public interface QueryHandler<Q extends Query<R>, R> {

    R handle(Q query);
}
