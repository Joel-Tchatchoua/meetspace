package com.accenture.meetspace.room.domain.vo;

import com.accenture.meetspace.room.domain.exceptions.InvalidCapacityException;

public record Capacity(int value) {

    public static final int MIN = 1;
    public static final int MAX = 200;

    public Capacity {
        if (value < MIN || value > MAX) {
            throw new InvalidCapacityException(value);
        }
    }
}
