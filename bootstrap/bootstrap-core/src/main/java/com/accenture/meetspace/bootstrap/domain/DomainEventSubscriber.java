package com.accenture.meetspace.bootstrap.domain;

public interface DomainEventSubscriber<E extends DomainEvent> {

    Class<E> subscribedTo();

    void on(E event);
}
