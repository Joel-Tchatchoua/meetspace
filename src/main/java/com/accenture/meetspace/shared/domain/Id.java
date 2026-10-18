package com.accenture.meetspace.shared.domain;

import java.util.Objects;
import java.util.UUID;

public abstract class Id {

    private final UUID value;

    protected Id(UUID value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    protected static UUID randomValue() {
        return UUID.randomUUID();
    }

    protected static UUID parse(String value) {
        return UUID.fromString(Objects.requireNonNull(value, "value"));
    }

    public final UUID value() {
        return value;
    }

    @Override
    public final boolean equals(Object other) {
        return this == other
                || other != null && getClass() == other.getClass() && value.equals(((Id) other).value);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), value);
    }

    @Override
    public final String toString() {
        return value.toString();
    }
}
