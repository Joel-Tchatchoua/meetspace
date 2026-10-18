package com.accenture.meetspace.room.units.stubs;

import com.accenture.meetspace.room.application.port.SiteRepository;
import com.accenture.meetspace.room.domain.entities.Site;
import com.accenture.meetspace.room.domain.vo.RoomId;
import com.accenture.meetspace.room.domain.vo.SiteId;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemorySiteRepository implements SiteRepository {

    private final Map<SiteId, Site> sites = new LinkedHashMap<>();

    @Override
    public void save(Site site) {
        sites.put(site.snapshot().id(), site);
    }

    @Override
    public Optional<Site> findById(SiteId id) {
        return Optional.ofNullable(sites.get(id)).filter(site -> !site.snapshot().deleted());
    }

    @Override
    public Optional<Site> findByRoomId(RoomId roomId) {
        return sites.values().stream()
                .filter(site -> !site.snapshot().deleted())
                .filter(site -> site.snapshot().room(roomId).isPresent())
                .findFirst();
    }

    @Override
    public boolean existsByCode(String code) {
        return sites.values().stream().anyMatch(site -> site.snapshot().code().equals(code));
    }

    @Override
    public boolean existsByName(String name) {
        return sites.values().stream().anyMatch(site -> site.snapshot().name().equals(name));
    }

    public Optional<Site> storedSite(SiteId id) {
        return Optional.ofNullable(sites.get(id));
    }

    public int count() {
        return sites.size();
    }
}
