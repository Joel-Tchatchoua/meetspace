package com.accenture.meetspace.bootstrap.domain;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {

    UUID eventId();

    Instant occurredOn();
}
