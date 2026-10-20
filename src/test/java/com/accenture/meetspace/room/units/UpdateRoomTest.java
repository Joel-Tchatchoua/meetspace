package com.accenture.meetspace.room.units;

import com.accenture.meetspace.room.application.command.update.UpdateRoomCommand;
import com.accenture.meetspace.room.application.command.update.UpdateRoomCommandHandler;
import com.accenture.meetspace.room.application.command.update.UpdateRoomResponse;
import com.accenture.meetspace.room.domain.entities.Site;
import com.accenture.meetspace.room.domain.enums.Amenity;
import com.accenture.meetspace.room.domain.exceptions.RoomCodeAlreadyUsedException;
import com.accenture.meetspace.room.domain.exceptions.RoomNotFoundException;
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

@DisplayName("Update room")
class UpdateRoomTest {

    private static final RoomId ROOM_ID = RoomId.generate();
    private static final Capacity EIGHT = new Capacity(8);
    private static final Set<Amenity> AMENITIES = Set.of(Amenity.TV_SCREEN);

    @Nested
    @DisplayName("command")
    class Command {

        @Nested
        @DisplayName("is valid")
        class Valid {

            @Test
            @DisplayName("with all its fields")
            void withAllItsFields() {
                var command = new UpdateRoomCommand(ROOM_ID, "R101", "Dodo", 1, EIGHT, AMENITIES);

                assertThat(command.roomId()).isEqualTo(ROOM_ID);
                assertThat(command.code()).isEqualTo("R101");
                assertThat(command.name()).isEqualTo("Dodo");
                assertThat(command.floor()).isEqualTo(1);
                assertThat(command.capacity()).isEqualTo(EIGHT);
                assertThat(command.amenities()).containsExactly(Amenity.TV_SCREEN);
            }

            @Test
            @DisplayName("without amenities")
            void withoutAmenities() {
                var command = new UpdateRoomCommand(ROOM_ID, "R101", "Dodo", 1, EIGHT, null);

                assertThat(command.amenities()).isEmpty();
            }
        }

        @Nested
        @DisplayName("is rejected")
        class Rejected {

            @Test
            @DisplayName("without room id")
            void withoutRoomId() {
                assertThatThrownBy(() -> new UpdateRoomCommand(null, "R101", "Dodo", 1, EIGHT, AMENITIES))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "roomId");
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = "  ")
            @DisplayName("without code")
            void withoutCode(String code) {
                assertThatThrownBy(() -> new UpdateRoomCommand(ROOM_ID, code, "Dodo", 1, EIGHT, AMENITIES))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "code");
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = "  ")
            @DisplayName("without name")
            void withoutName(String name) {
                assertThatThrownBy(() -> new UpdateRoomCommand(ROOM_ID, "R101", name, 1, EIGHT, AMENITIES))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "name");
            }

            @Test
            @DisplayName("without floor")
            void withoutFloor() {
                assertThatThrownBy(() -> new UpdateRoomCommand(ROOM_ID, "R101", "Dodo", null, EIGHT, AMENITIES))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "floor");
            }

            @Test
            @DisplayName("without capacity")
            void withoutCapacity() {
                assertThatThrownBy(() -> new UpdateRoomCommand(ROOM_ID, "R101", "Dodo", 1, null, AMENITIES))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "capacity");
            }
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemorySiteRepository siteRepository = new InMemorySiteRepository();
        private final UpdateRoomCommandHandler handler = new UpdateRoomCommandHandler(siteRepository);

        private final SiteId ebeneId = SiteId.generate();
        private final RoomId dodoId = RoomId.generate();

        @BeforeEach
        void givenSitesWithRooms() {
            var mauritius = ZoneId.of("Indian/Mauritius");
            var ebene = Site.create(ebeneId, "EBN", "Ebène", "Cybercity, Ebène", mauritius);
            ebene.addRoom(dodoId, "R101", "Dodo", 1, EIGHT, Set.of(Amenity.PROJECTOR));
            ebene.addRoom(RoomId.generate(), "R102", "Kestrel", 1, new Capacity(4), Set.of());
            siteRepository.save(ebene);
            var portLouis = Site.create(SiteId.generate(), "PLU", "Port-Louis", "Caudan", mauritius);
            portLouis.addRoom(RoomId.generate(), "R201", "Pink Pigeon", 2, new Capacity(6), Set.of());
            siteRepository.save(portLouis);
        }

        private Site ebene() {
            return siteRepository.findById(ebeneId).orElseThrow();
        }

        @Nested
        @DisplayName("succeeds")
        class Succeeds {

            @Test
            @DisplayName("returning the updated room id")
            void returningTheUpdatedRoomId() {
                var response = handler.handle(new UpdateRoomCommand(dodoId, "R101", "Dodo XL", 1, EIGHT, AMENITIES));

                assertThat(response).isEqualTo(new UpdateRoomResponse(dodoId.value(), true));
            }

            @Test
            @DisplayName("replacing every detail of the room")
            void replacingEveryDetailOfTheRoom() {
                handler.handle(new UpdateRoomCommand(dodoId, "R103", "Dodo XL", 3, new Capacity(20),
                        Set.of(Amenity.VIDEO_CONFERENCE)));

                var room = ebene().snapshot().room(dodoId).orElseThrow();
                assertThat(room.code()).isEqualTo("R103");
                assertThat(room.name()).isEqualTo("Dodo XL");
                assertThat(room.floor()).isEqualTo(3);
                assertThat(room.capacity()).isEqualTo(new Capacity(20));
                assertThat(room.amenities()).containsExactly(Amenity.VIDEO_CONFERENCE);
            }

            @Test
            @DisplayName("keeping its own code")
            void keepingItsOwnCode() {
                handler.handle(new UpdateRoomCommand(dodoId, "R101", "Dodo XL", 1, EIGHT, AMENITIES));

                var room = ebene().snapshot().room(dodoId).orElseThrow();
                assertThat(room.code()).isEqualTo("R101");
                assertThat(room.name()).isEqualTo("Dodo XL");
            }

            @Test
            @DisplayName("taking a code used in another site")
            void takingACodeUsedInAnotherSite() {
                handler.handle(new UpdateRoomCommand(dodoId, "R201", "Dodo", 1, EIGHT, AMENITIES));

                assertThat(ebene().snapshot().room(dodoId).orElseThrow().code()).isEqualTo("R201");
            }
        }

        @Nested
        @DisplayName("fails")
        class Fails {

            @Test
            @DisplayName("when the room does not exist")
            void whenTheRoomDoesNotExist() {
                var unknown = new UpdateRoomCommand(RoomId.generate(), "R999", "Ghost", 1, EIGHT, AMENITIES);

                assertThatThrownBy(() -> handler.handle(unknown))
                        .isInstanceOf(RoomNotFoundException.class)
                        .hasFieldOrPropertyWithValue("code", "ROOM_NOT_FOUND");
            }

            @Test
            @DisplayName("when the room is removed")
            void whenTheRoomIsRemoved() {
                ebene().removeRoom(dodoId);

                assertThatThrownBy(() -> handler.handle(new UpdateRoomCommand(dodoId, "R101", "Dodo", 1, EIGHT, AMENITIES)))
                        .isInstanceOf(RoomNotFoundException.class)
                        .hasFieldOrPropertyWithValue("code", "ROOM_NOT_FOUND");
            }

            @Test
            @DisplayName("when the code is used by another room of the site")
            void whenTheCodeIsUsedByAnotherRoomOfTheSite() {
                assertThatThrownBy(() -> handler.handle(new UpdateRoomCommand(dodoId, "R102", "Dodo", 1, EIGHT, AMENITIES)))
                        .isInstanceOf(RoomCodeAlreadyUsedException.class)
                        .hasFieldOrPropertyWithValue("code", "ROOM_CODE_ALREADY_USED");
                assertThat(ebene().snapshot().room(dodoId).orElseThrow().code()).isEqualTo("R101");
            }
        }
    }
}
