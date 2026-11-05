package com.accenture.meetspace.bootstrap.application.query;

public interface QueryBus {

    <R> R ask(Query<R> query);
}
