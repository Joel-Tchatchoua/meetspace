package com.accenture.meetspace.bootstrap.domain;

import java.util.List;

public interface DomainEventPublisher {

    void publish(List<DomainEvent> events);
}
