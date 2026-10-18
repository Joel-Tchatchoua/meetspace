package com.accenture.meetspace.room.domain.vo;

import com.accenture.meetspace.shared.domain.Id;

import java.util.UUID;

public final class SiteId extends Id {

    private SiteId(UUID value) {
        super(value);
    }

    public static SiteId generate() {
        return new SiteId(randomValue());
    }

    public static SiteId of(UUID value) {
        return new SiteId(value);
    }
}
