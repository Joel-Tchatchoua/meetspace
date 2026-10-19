package com.accenture.meetspace.room.application.port.out;

import com.accenture.meetspace.room.domain.entities.Site;
import com.accenture.meetspace.room.domain.vo.RoomId;
import com.accenture.meetspace.room.domain.vo.SiteId;

import java.util.Optional;

public interface SiteRepository {

    void save(Site site);

    Optional<Site> findById(SiteId id);

    Optional<Site> findByRoomId(RoomId roomId);

    boolean existsByCode(String code);

    boolean existsByName(String name);
}
