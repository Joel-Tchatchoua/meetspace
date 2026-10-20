package com.accenture.meetspace.room.infrastructure.adapters.out.persistence.repositories;

import com.accenture.meetspace.room.application.port.out.repository.SiteRepository;
import com.accenture.meetspace.room.domain.entities.Site;
import com.accenture.meetspace.room.domain.vo.RoomId;
import com.accenture.meetspace.room.domain.vo.SiteId;
import com.accenture.meetspace.room.infrastructure.adapters.out.persistence.mappers.SiteModelMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaSiteRepository implements SiteRepository {

    private final SpringDataSiteRepository jpaRepository;
    private final SiteModelMapper mapper;

    public JpaSiteRepository(SpringDataSiteRepository jpaRepository, SiteModelMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(Site site) {
        var snapshot = site.snapshot();
        jpaRepository.findById(snapshot.id().value().toString()).ifPresentOrElse(
                model -> mapper.update(model, snapshot),
                () -> jpaRepository.save(mapper.toNewModel(snapshot)));
    }

    @Override
    public Optional<Site> findById(SiteId id) {
        return jpaRepository.findForUpdateById(id.value().toString()).map(mapper::toDomain);
    }

    @Override
    public Optional<Site> findByRoomId(RoomId roomId) {
        return jpaRepository.findForUpdateByRoomId(roomId.value().toString()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
}
