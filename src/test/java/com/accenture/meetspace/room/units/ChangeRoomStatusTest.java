package com.accenture.meetspace.room.units;

import com.accenture.meetspace.room.application.command.changestatus.ChangeRoomStatusCommand;
import com.accenture.meetspace.room.application.command.changestatus.ChangeRoomStatusCommandHandler;
import com.accenture.meetspace.room.application.command.changestatus.ChangeRoomStatusResponse;
import com.accenture.meetspace.room.domain.entities.Site;
import com.accenture.meetspace.room.domain.enums.RoomStatus;
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
import org.junit.jupiter.params.provider.EnumSource;

import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Change room status")
class ChangeRoomStatusTest {

    @Nested
    @DisplayName("command")
    class Command {

        @Test
        @DisplayName("is valid with a room id and a status")
        void isValidWithARoomIdAndAStatus() {
            var roomId = RoomId.generate();

            var command = new ChangeRoomStatusCommand(roomId, RoomStatus.MAINTENANCE);

            assertThat(command.roomId()).isEqualTo(roomId);
            assertThat(command.status()).isEqualTo(RoomStatus.MAINTENANCE);
        }

        @Test
        @DisplayName("is rejected without room id")
        void isRejectedWithoutRoomId() {
            assertThatThrownBy(() -> new ChangeRoomStatusCommand(null, RoomStatus.MAINTENANCE))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "roomId");
        }

        @Test
        @DisplayName("is rejected without status")
        void isRejectedWithoutStatus() {
            assertThatThrownBy(() -> new ChangeRoomStatusCommand(RoomId.generate(), null))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "status");
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemorySiteRepository siteRepository = new InMemorySiteRepository();
        private final ChangeRoomStatusCommandHandler handler = new ChangeRoomStatusCommandHandler(siteRepository);

        private final SiteId ebeneId = SiteId.generate();
        private final RoomId dodoId = RoomId.generate();

        @BeforeEach
        void givenASiteWithARoom() {
            var ebene = Site.create(ebeneId, "EBN", "Ebène", "Cybercity, Ebène", ZoneId.of("Indian/Mauritius"));
            ebene.addRoom(dodoId, "R101", "Dodo", 1, new Capacity(8), Set.of());
            siteRepository.save(ebene);
        }

        private Site ebene() {
            return siteRepository.findById(ebeneId).orElseThrow();
        }

        @Nested
        @DisplayName("succeeds")
        class Succeeds {

            @Test
            @DisplayName("returning the room id and its new status")
            void returningTheRoomIdAndItsNewStatus() {
                var response = handler.handle(new ChangeRoomStatusCommand(dodoId, RoomStatus.MAINTENANCE));

                assertThat(response)
                        .isEqualTo(new ChangeRoomStatusResponse(dodoId.value(), RoomStatus.MAINTENANCE, true));
            }

            @ParameterizedTest
            @EnumSource(RoomStatus.class)
            @DisplayName("moving the room to the requested status")
            void movingTheRoomToTheRequestedStatus(RoomStatus status) {
                handler.handle(new ChangeRoomStatusCommand(dodoId, status));

                assertThat(ebene().snapshot().room(dodoId).orElseThrow().status()).isEqualTo(status);
            }

            @Test
            @DisplayName("reactivating a room under maintenance")
            void reactivatingARoomUnderMaintenance() {
                handler.handle(new ChangeRoomStatusCommand(dodoId, RoomStatus.MAINTENANCE));

                handler.handle(new ChangeRoomStatusCommand(dodoId, RoomStatus.ACTIVE));

                assertThat(ebene().snapshot().room(dodoId).orElseThrow().status()).isEqualTo(RoomStatus.ACTIVE);
            }
        }

        @Nested
        @DisplayName("fails")
        class Fails {

            @Test
            @DisplayName("when the room does not exist")
            void whenTheRoomDoesNotExist() {
                assertThatThrownBy(() -> handler.handle(new ChangeRoomStatusCommand(RoomId.generate(), RoomStatus.CLOSED)))
                        .isInstanceOf(RoomNotFoundException.class)
                        .hasFieldOrPropertyWithValue("code", "ROOM_NOT_FOUND");
            }

            @Test
            @DisplayName("when the room is removed")
            void whenTheRoomIsRemoved() {
                ebene().removeRoom(dodoId);

                assertThatThrownBy(() -> handler.handle(new ChangeRoomStatusCommand(dodoId, RoomStatus.CLOSED)))
                        .isInstanceOf(RoomNotFoundException.class)
                        .hasFieldOrPropertyWithValue("code", "ROOM_NOT_FOUND");
            }
        }
    }
}
