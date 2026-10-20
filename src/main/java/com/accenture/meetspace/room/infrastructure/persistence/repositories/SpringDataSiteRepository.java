package com.accenture.meetspace.room.infrastructure.persistence.repositories;

import com.accenture.meetspace.room.infrastructure.persistence.models.SiteModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataSiteRepository extends JpaRepository<SiteModel, String> {

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select s from SiteModel s where s.id = :id")
    Optional<SiteModel> findForUpdateById(@Param("id") String id);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select s from SiteModel s join s.rooms r where r.id = :roomId")
    Optional<SiteModel> findForUpdateByRoomId(@Param("roomId") String roomId);

    boolean existsByCode(String code);

    boolean existsByName(String name);
}
