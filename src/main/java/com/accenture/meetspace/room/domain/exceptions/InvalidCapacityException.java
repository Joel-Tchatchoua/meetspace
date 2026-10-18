package com.accenture.meetspace.room.domain.exceptions;

import com.accenture.meetspace.room.domain.vo.Capacity;
import com.accenture.meetspace.shared.domain.DomainValidationException;

public class InvalidCapacityException extends DomainValidationException {

    public InvalidCapacityException(int value) {
        super("INVALID_CAPACITY",
                "Capacity must be between " + Capacity.MIN + " and " + Capacity.MAX + ", got " + value);
    }
}
