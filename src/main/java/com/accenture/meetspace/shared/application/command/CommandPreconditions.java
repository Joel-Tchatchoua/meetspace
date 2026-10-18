package com.accenture.meetspace.shared.application.command;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Set;

public final class CommandPreconditions {

    private CommandPreconditions() {
    }

    public static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new InvalidCommandException(field, "must not be null");
        }
        return value;
    }

    public static String requireNotBlank(String value, String field) {
        if (isBlank(value)) {
            throw new InvalidCommandException(field, "must not be blank");
        }
        return value;
    }

    public static <T> Set<T> copyOrEmpty(Set<T> values) {
        return values == null ? Set.of() : Set.copyOf(values);
    }

    public static String requireTimeZoneOrDefault(String value, String defaultValue, String field) {
        if (isBlank(value)) {
            return defaultValue;
        }
        try {
            return ZoneId.of(value).getId();
        } catch (DateTimeException e) {
            throw new InvalidCommandException(field, "unknown time zone " + value);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
