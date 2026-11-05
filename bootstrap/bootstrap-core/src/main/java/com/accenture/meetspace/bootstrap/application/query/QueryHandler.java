package com.accenture.meetspace.bootstrap.application.query;

@FunctionalInterface
public interface QueryHandler<Q extends Query<R>, R> {

    R handle(Q query);
}
