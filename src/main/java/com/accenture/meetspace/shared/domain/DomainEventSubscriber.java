package com.accenture.meetspace.shared.domain;

public interface DomainEventSubscriber<E extends DomainEvent> {

    Class<E> subscribedTo();

    void on(E event);
}
