package com.accenture.meetspace.shared.application.query;

public interface QueryBus {

    <R> R ask(Query<R> query);
}
