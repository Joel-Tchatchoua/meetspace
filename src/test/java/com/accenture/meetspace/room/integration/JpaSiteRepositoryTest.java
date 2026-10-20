package com.accenture.meetspace.room.integration;

import com.accenture.meetspace.room.domain.entities.Site;
import com.accenture.meetspace.room.domain.enums.Amenity;
import com.accenture.meetspace.room.domain.vo.Capacity;
import com.accenture.meetspace.room.domain.vo.RoomId;
import com.accenture.meetspace.room.domain.vo.SiteId;
import com.accenture.meetspace.room.infrastructure.persistence.adapters.out.JpaSiteRepository;
import com.accenture.meetspace.room.infrastructure.persistence.mappers.SiteModelMapper;
import com.accenture.meetspace.room.infrastructure.persistence.repositories.SpringDataSiteRepository;
import com.accenture.meetspace.shared.integration.JpaIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JpaIntegrationTest
@Import({JpaSiteRepository.class, SiteModelMapper.class})
@DisplayName("JpaSiteRepository")
class JpaSiteRepositoryTest {

    private static final ZoneId MAURITIUS = ZoneId.of("Indian/Mauritius");

    @Autowired
    JpaSiteRepository repository;

    @Autowired
    SpringDataSiteRepository jpaRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    PlatformTransactionManager transactionManager;

    private static Site ebene() {
        return Site.create(SiteId.generate(), "EBN", "Ebène", "Cybercity, Ebène", MAURITIUS);
    }

    private static Site portLouis() {
        return Site.create(SiteId.generate(), "PLU", "Port-Louis", "Caudan", MAURITIUS);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private Site reload(SiteId siteId) {
        flushAndClear();
        return repository.findById(siteId).orElseThrow();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persists a new site with its rooms")
        void persistsANewSiteWithItsRooms() {
            var site = ebene();
            var dodoId = RoomId.generate();
            var kestrelId = RoomId.generate();
            site.addRoom(dodoId, "R101", "Dodo", 1, new Capacity(8), Set.of(Amenity.PROJECTOR, Amenity.WHITEBOARD));
            site.addRoom(kestrelId, "B01", "Kestrel", -1, new Capacity(4), Set.of());
            site.putRoomUnderMaintenance(dodoId);
            site.removeRoom(kestrelId);

            repository.save(site);

            var reloaded = reload(site.snapshot().id()).snapshot();
            assertThat(reloaded)
                    .usingRecursiveComparison()
                    .ignoringFields("rooms")
                    .isEqualTo(site.snapshot());
            assertThat(reloaded.rooms())
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(site.snapshot().room(dodoId).orElseThrow());
        }

        @Test
        @DisplayName("keeps a deleted site in the database with its deletion instant")
        void keepsADeletedSiteInTheDatabaseWithItsDeletionInstant() {
            var site = ebene();
            site.delete();

            repository.save(site);
            flushAndClear();

            var deletedAt = entityManager
                    .createNativeQuery("select deleted_at from sites where id = :id")
                    .setParameter("id", site.snapshot().id().value().toString())
                    .getSingleResult();
            assertThat(deletedAt).isNotNull();
        }

        @Test
        @DisplayName("keeps a removed room in the database with its deletion instant")
        void keepsARemovedRoomInTheDatabaseWithItsDeletionInstant() {
            var site = ebene();
            var dodoId = RoomId.generate();
            site.addRoom(dodoId, "R101", "Dodo", 1, new Capacity(8), Set.of());
            site.removeRoom(dodoId);

            repository.save(site);
            flushAndClear();

            var deletedAt = entityManager
                    .createNativeQuery("select deleted_at from meeting_rooms where id = :id")
                    .setParameter("id", dodoId.value().toString())
                    .getSingleResult();
            assertThat(deletedAt).isNotNull();
        }

        @Test
        @DisplayName("persists the changes of an existing site and its rooms")
        void persistsTheChangesOfAnExistingSiteAndItsRooms() {
            var site = ebene();
            var dodoId = RoomId.generate();
            site.addRoom(dodoId, "R101", "Dodo", 1, new Capacity(8), Set.of(Amenity.PROJECTOR));
            repository.save(site);
            var loaded = reload(site.snapshot().id());

            loaded.changeDetails("EBX", "Ebène Tower", "Tower 1, Ebène", ZoneId.of("Europe/Paris"));
            loaded.changeRoomDetails(dodoId, "R103", "Dodo XL", 3, new Capacity(20), Set.of(Amenity.TV_SCREEN));
            loaded.closeRoom(dodoId);
            loaded.addRoom(RoomId.generate(), "R104", "Pink Pigeon", 4, new Capacity(6), Set.of());
            repository.save(loaded);

            assertThat(reload(site.snapshot().id()).snapshot())
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(loaded.snapshot());
        }

        @Test
        @DisplayName("stamps the creation and update instants and starts at version 0")
        void stampsTheCreationAndUpdateInstantsAndStartsAtVersion0() {
            var site = ebene();

            repository.save(site);
            flushAndClear();

            var model = jpaRepository.findById(site.snapshot().id().value().toString()).orElseThrow();
            assertThat(model.getCreatedAt()).isNotNull();
            assertThat(model.getUpdatedAt()).isNotNull();
            assertThat(model.getVersion()).isZero();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("finds nothing for an unknown site")
        void findsNothingForAnUnknownSite() {
            assertThat(repository.findById(SiteId.generate())).isEmpty();
        }

        @Test
        @DisplayName("ignores a deleted site")
        void ignoresADeletedSite() {
            var site = ebene();
            site.delete();
            repository.save(site);
            flushAndClear();

            assertThat(repository.findById(site.snapshot().id())).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByRoomId")
    class FindByRoomId {

        @Test
        @DisplayName("finds the site holding the room")
        void findsTheSiteHoldingTheRoom() {
            var site = ebene();
            var dodoId = RoomId.generate();
            site.addRoom(dodoId, "R101", "Dodo", 1, new Capacity(8), Set.of());
            repository.save(site);
            repository.save(portLouis());
            flushAndClear();

            assertThat(repository.findByRoomId(dodoId))
                    .hasValueSatisfying(found -> assertThat(found.snapshot().id()).isEqualTo(site.snapshot().id()));
        }

        @Test
        @DisplayName("finds nothing for an unknown room")
        void findsNothingForAnUnknownRoom() {
            repository.save(ebene());
            flushAndClear();

            assertThat(repository.findByRoomId(RoomId.generate())).isEmpty();
        }

        @Test
        @DisplayName("ignores a removed room")
        void ignoresARemovedRoom() {
            var site = ebene();
            var dodoId = RoomId.generate();
            site.addRoom(dodoId, "R101", "Dodo", 1, new Capacity(8), Set.of());
            site.removeRoom(dodoId);
            repository.save(site);
            flushAndClear();

            assertThat(repository.findByRoomId(dodoId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByCode and existsByName")
    class Exists {

        @Test
        @DisplayName("are true for an existing site")
        void areTrueForAnExistingSite() {
            repository.save(ebene());
            flushAndClear();

            assertThat(repository.existsByCode("EBN")).isTrue();
            assertThat(repository.existsByName("Ebène")).isTrue();
        }

        @Test
        @DisplayName("are false for a deleted site")
        void areFalseForADeletedSite() {
            var site = ebene();
            site.delete();
            repository.save(site);
            flushAndClear();

            assertThat(repository.existsByCode("EBN")).isFalse();
            assertThat(repository.existsByName("Ebène")).isFalse();
        }

        @Test
        @DisplayName("are false for an unknown code or name")
        void areFalseForAnUnknownCodeOrName() {
            repository.save(ebene());
            flushAndClear();

            assertThat(repository.existsByCode("PLU")).isFalse();
            assertThat(repository.existsByName("Port-Louis")).isFalse();
        }
    }

    @Nested
    @DisplayName("database constraints")
    class Constraints {

        @Test
        @DisplayName("reject two sites with the same code")
        void rejectTwoSitesWithTheSameCode() {
            repository.save(ebene());
            repository.save(Site.create(SiteId.generate(), "EBN", "Other", "Elsewhere", MAURITIUS));

            assertThatThrownBy(() -> jpaRepository.flush()).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("reject two sites with the same name")
        void rejectTwoSitesWithTheSameName() {
            repository.save(ebene());
            repository.save(Site.create(SiteId.generate(), "OTH", "Ebène", "Elsewhere", MAURITIUS));

            assertThatThrownBy(() -> jpaRepository.flush()).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("reject two active rooms with the same code in a site")
        void rejectTwoActiveRoomsWithTheSameCodeInASite() {
            var site = ebene();
            site.addRoom(RoomId.generate(), "R101", "Dodo", 1, new Capacity(8), Set.of());
            repository.save(site);
            flushAndClear();

            assertThatThrownBy(() -> entityManager
                    .createNativeQuery("""
                            insert into meeting_rooms
                                (id, site_id, code, name, floor, capacity, status, version, created_at, updated_at)
                            values (:id, :siteId, 'R101', 'Twin', 1, 8, 'ACTIVE', 0, now(6), now(6))
                            """)
                    .setParameter("id", RoomId.generate().value().toString())
                    .setParameter("siteId", site.snapshot().id().value().toString())
                    .executeUpdate())
                    .hasRootCauseInstanceOf(SQLIntegrityConstraintViolationException.class);
        }

        @Test
        @DisplayName("allow reusing the code and name of a deleted site")
        void allowReusingTheCodeAndNameOfADeletedSite() {
            var deleted = ebene();
            deleted.delete();
            repository.save(deleted);
            flushAndClear();
            var site = ebene();

            repository.save(site);
            jpaRepository.flush();

            assertThat(repository.findById(site.snapshot().id())).isPresent();
        }

        @Test
        @DisplayName("allow reusing the code of a removed room in its site")
        void allowReusingTheCodeOfARemovedRoomInItsSite() {
            var site = ebene();
            var removedId = RoomId.generate();
            site.addRoom(removedId, "R101", "Dodo", 1, new Capacity(8), Set.of());
            site.removeRoom(removedId);
            repository.save(site);
            var loaded = reload(site.snapshot().id());
            var dodoId = RoomId.generate();

            loaded.addRoom(dodoId, "R101", "Dodo", 1, new Capacity(8), Set.of());
            repository.save(loaded);

            assertThat(reload(site.snapshot().id()).snapshot().room(dodoId)).isPresent();
        }
    }

    @Nested
    @DisplayName("optimistic locking")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    class OptimisticLocking {

        private TransactionTemplate inTransaction() {
            return new TransactionTemplate(transactionManager);
        }

        private TransactionTemplate inNewTransaction() {
            var template = new TransactionTemplate(transactionManager);
            template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            return template;
        }

        @Test
        @DisplayName("increments the site version when only one of its rooms changes")
        void incrementsTheSiteVersionWhenOnlyOneOfItsRoomsChanges() {
            var site = ebene();
            var siteId = site.snapshot().id();
            inTransaction().executeWithoutResult(status -> repository.save(site));

            inTransaction().executeWithoutResult(status -> {
                var loaded = repository.findById(siteId).orElseThrow();
                loaded.addRoom(RoomId.generate(), "R101", "Dodo", 1, new Capacity(8), Set.of());
                repository.save(loaded);
            });

            var model = jpaRepository.findById(siteId.value().toString()).orElseThrow();
            assertThat(model.getVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("rejects saving a site changed meanwhile by another transaction")
        void rejectsSavingASiteChangedMeanwhileByAnotherTransaction() {
            var site = ebene();
            var siteId = site.snapshot().id();
            inTransaction().executeWithoutResult(status -> repository.save(site));

            assertThatThrownBy(() -> inTransaction().executeWithoutResult(status -> {
                var stale = repository.findById(siteId).orElseThrow();
                inNewTransaction().executeWithoutResult(other -> {
                    var fresh = repository.findById(siteId).orElseThrow();
                    fresh.changeDetails("EBX", "Ebène Tower", "Tower 1", MAURITIUS);
                    repository.save(fresh);
                });
                stale.changeDetails("EBY", "Ebène Heights", "Tower 2", MAURITIUS);
                repository.save(stale);
            })).isInstanceOf(ObjectOptimisticLockingFailureException.class);
        }

        @Test
        @DisplayName("rejects deleting a site that got a room meanwhile")
        void rejectsDeletingASiteThatGotARoomMeanwhile() {
            var site = ebene();
            var siteId = site.snapshot().id();
            inTransaction().executeWithoutResult(status -> repository.save(site));

            assertThatThrownBy(() -> inTransaction().executeWithoutResult(status -> {
                var stale = repository.findById(siteId).orElseThrow();
                inNewTransaction().executeWithoutResult(other -> {
                    var fresh = repository.findById(siteId).orElseThrow();
                    fresh.addRoom(RoomId.generate(), "R101", "Dodo", 1, new Capacity(8), Set.of());
                    repository.save(fresh);
                });
                stale.delete();
                repository.save(stale);
            })).isInstanceOf(ObjectOptimisticLockingFailureException.class);
        }
    }
}
