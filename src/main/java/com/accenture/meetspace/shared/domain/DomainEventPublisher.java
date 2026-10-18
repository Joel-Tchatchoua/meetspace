package com.accenture.meetspace.shared.domain;

import java.util.List;

public interface DomainEventPublisher {

    void publish(List<DomainEvent> events);
}
