package com.accenture.meetspace.room.units;

import com.accenture.meetspace.room.application.command.create.CreateRoomCommand;
import com.accenture.meetspace.room.application.command.create.CreateRoomCommandHandler;
import com.accenture.meetspace.room.domain.entities.Site;
import com.accenture.meetspace.room.domain.enums.Amenity;
import com.accenture.meetspace.room.domain.enums.RoomStatus;
import com.accenture.meetspace.room.domain.exceptions.RoomCodeAlreadyUsedException;
import com.accenture.meetspace.room.domain.exceptions.SiteNotFoundException;
import com.accenture.meetspace.room.domain.vo.Capacity;
import com.accenture.meetspace.room.domain.vo.RoomId;
import com.accenture.meetspace.room.domain.vo.SiteId;
import com.accenture.meetspace.room.units.stubs.InMemorySiteRepository;
import com.accenture.meetspace.shared.application.command.InvalidCommandException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Create room")
class CreateRoomTest {

    private static final RoomId ROOM_ID = RoomId.generate();
    private static final SiteId SITE_ID = SiteId.generate();
    private static final Capacity EIGHT = new Capacity(8);
    private static final Set<Amenity> AMENITIES = Set.of(Amenity.PROJECTOR, Amenity.WHITEBOARD);

    @Nested
    @DisplayName("command")
    class Command {

        @Nested
        @DisplayName("is valid")
        class Valid {

            @Test
            @DisplayName("with all its fields")
            void withAllItsFields() {
                var command = new CreateRoomCommand(ROOM_ID, SITE_ID, "R101", "Dodo", 1, EIGHT, AMENITIES);

                assertThat(command.roomId()).isEqualTo(ROOM_ID);
                assertThat(command.siteId()).isEqualTo(SITE_ID);
                assertThat(command.code()).isEqualTo("R101");
                assertThat(command.name()).isEqualTo("Dodo");
                assertThat(command.floor()).isEqualTo(1);
                assertThat(command.capacity()).isEqualTo(EIGHT);
                assertThat(command.amenities()).containsExactlyInAnyOrder(Amenity.PROJECTOR, Amenity.WHITEBOARD);
            }

            @Test
            @DisplayName("with a floor below ground")
            void withAFloorBelowGround() {
                var command = new CreateRoomCommand(ROOM_ID, SITE_ID, "B01", "Bunker", -1, EIGHT, AMENITIES);

                assertThat(command.floor()).isEqualTo(-1);
            }

            @Test
            @DisplayName("without amenities")
            void withoutAmenities() {
                var command = new CreateRoomCommand(ROOM_ID, SITE_ID, "R101", "Dodo", 1, EIGHT, null);

                assertThat(command.amenities()).isEmpty();
            }
        }

        @Nested
        @DisplayName("is rejected")
        class Rejected {

            @Test
            @DisplayName("without room id")
            void withoutRoomId() {
                assertThatThrownBy(() -> new CreateRoomCommand(null, SITE_ID, "R101", "Dodo", 1, EIGHT, AMENITIES))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "roomId");
            }

            @Test
            @DisplayName("without site id")
            void withoutSiteId() {
                assertThatThrownBy(() -> new CreateRoomCommand(ROOM_ID, null, "R101", "Dodo", 1, EIGHT, AMENITIES))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "siteId");
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = "  ")
            @DisplayName("without code")
            void withoutCode(String code) {
                assertThatThrownBy(() -> new CreateRoomCommand(ROOM_ID, SITE_ID, code, "Dodo", 1, EIGHT, AMENITIES))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "code");
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = "  ")
            @DisplayName("without name")
            void withoutName(String name) {
                assertThatThrownBy(() -> new CreateRoomCommand(ROOM_ID, SITE_ID, "R101", name, 1, EIGHT, AMENITIES))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "name");
            }

            @Test
            @DisplayName("without floor")
            void withoutFloor() {
                assertThatThrownBy(() -> new CreateRoomCommand(ROOM_ID, SITE_ID, "R101", "Dodo", null, EIGHT, AMENITIES))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "floor");
            }

            @Test
            @DisplayName("without capacity")
            void withoutCapacity() {
                assertThatThrownBy(() -> new CreateRoomCommand(ROOM_ID, SITE_ID, "R101", "Dodo", 1, null, AMENITIES))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "capacity");
            }
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemorySiteRepository siteRepository = new InMemorySiteRepository();
        private final CreateRoomCommandHandler handler = new CreateRoomCommandHandler(siteRepository);

        private final SiteId ebeneId = SiteId.generate();
        private final SiteId portLouisId = SiteId.generate();

        @BeforeEach
        void givenTwoSites() {
            var mauritius = ZoneId.of("Indian/Mauritius");
            siteRepository.save(Site.create(ebeneId, "EBN", "Ebène", "Cybercity, Ebène", mauritius));
            siteRepository.save(Site.create(portLouisId, "PLU", "Port-Louis", "Caudan", mauritius));
        }

        private CreateRoomCommand dodoIn(SiteId siteId) {
            return new CreateRoomCommand(RoomId.generate(), siteId, "R101", "Dodo", 1, EIGHT, AMENITIES);
        }

        private Site site(SiteId siteId) {
            return siteRepository.findById(siteId).orElseThrow();
        }

        @Nested
        @DisplayName("succeeds")
        class Succeeds {

            @Test
            @DisplayName("adding an active room to the site")
            void addingAnActiveRoomToTheSite() {
                var command = dodoIn(ebeneId);

                handler.handle(command);

                var room = site(ebeneId).snapshot().room(command.roomId()).orElseThrow();
                assertThat(room.code()).isEqualTo("R101");
                assertThat(room.name()).isEqualTo("Dodo");
                assertThat(room.floor()).isEqualTo(1);
                assertThat(room.capacity()).isEqualTo(EIGHT);
                assertThat(room.amenities()).containsExactlyInAnyOrder(Amenity.PROJECTOR, Amenity.WHITEBOARD);
                assertThat(room.status()).isEqualTo(RoomStatus.ACTIVE);
                assertThat(room.deleted()).isFalse();
            }

            @Test
            @DisplayName("reusing a code taken in another site")
            void reusingACodeTakenInAnotherSite() {
                handler.handle(dodoIn(portLouisId));
                var command = dodoIn(ebeneId);

                handler.handle(command);

                assertThat(site(ebeneId).snapshot().room(command.roomId())).isPresent();
            }
        }

        @Nested
        @DisplayName("fails")
        class Fails {

            @Test
            @DisplayName("when the site does not exist")
            void whenTheSiteDoesNotExist() {
                assertThatThrownBy(() -> handler.handle(dodoIn(SiteId.generate())))
                        .isInstanceOf(SiteNotFoundException.class)
                        .hasFieldOrPropertyWithValue("code", "SITE_NOT_FOUND");
            }

            @Test
            @DisplayName("when the site is deleted")
            void whenTheSiteIsDeleted() {
                site(ebeneId).delete();

                assertThatThrownBy(() -> handler.handle(dodoIn(ebeneId)))
                        .isInstanceOf(SiteNotFoundException.class)
                        .hasFieldOrPropertyWithValue("code", "SITE_NOT_FOUND");
                assertThat(siteRepository.storedSite(ebeneId).orElseThrow().snapshot().rooms()).isEmpty();
            }

            @Test
            @DisplayName("when the code is already used in the site")
            void whenTheCodeIsAlreadyUsedInTheSite() {
                handler.handle(dodoIn(ebeneId));

                assertThatThrownBy(() -> handler.handle(dodoIn(ebeneId)))
                        .isInstanceOf(RoomCodeAlreadyUsedException.class)
                        .hasFieldOrPropertyWithValue("code", "ROOM_CODE_ALREADY_USED");
                assertThat(site(ebeneId).snapshot().rooms()).hasSize(1);
            }

            @Test
            @DisplayName("when the code belongs to a removed room of the site")
            void whenTheCodeBelongsToARemovedRoomOfTheSite() {
                var removed = dodoIn(ebeneId);
                handler.handle(removed);
                site(ebeneId).removeRoom(removed.roomId());

                assertThatThrownBy(() -> handler.handle(dodoIn(ebeneId)))
                        .isInstanceOf(RoomCodeAlreadyUsedException.class)
                        .hasFieldOrPropertyWithValue("code", "ROOM_CODE_ALREADY_USED");
            }
        }
    }
}
