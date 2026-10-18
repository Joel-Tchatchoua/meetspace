package com.accenture.meetspace.room.units;

import com.accenture.meetspace.room.application.command.delete.DeleteRoomCommand;
import com.accenture.meetspace.room.application.command.delete.DeleteRoomCommandHandler;
import com.accenture.meetspace.room.domain.entities.MeetingRoom;
import com.accenture.meetspace.room.domain.entities.Site;
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

import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Delete room")
class DeleteRoomTest {

    @Nested
    @DisplayName("command")
    class Command {

        @Test
        @DisplayName("is valid with a room id")
        void isValidWithARoomId() {
            var roomId = RoomId.generate();

            assertThat(new DeleteRoomCommand(roomId).roomId()).isEqualTo(roomId);
        }

        @Test
        @DisplayName("is rejected without room id")
        void isRejectedWithoutRoomId() {
            assertThatThrownBy(() -> new DeleteRoomCommand(null))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "roomId");
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemorySiteRepository siteRepository = new InMemorySiteRepository();
        private final DeleteRoomCommandHandler handler = new DeleteRoomCommandHandler(siteRepository);

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
            @DisplayName("removing the room from its site")
            void removingTheRoomFromItsSite() {
                handler.handle(new DeleteRoomCommand(dodoId));

                assertThat(ebene().snapshot().room(dodoId))
                        .hasValueSatisfying(room -> assertThat(room.deleted()).isTrue());
                assertThat(ebene().snapshot().rooms()).allMatch(MeetingRoom.Snapshot::deleted);
            }
        }

        @Nested
        @DisplayName("fails")
        class Fails {

            @Test
            @DisplayName("when the room does not exist")
            void whenTheRoomDoesNotExist() {
                assertThatThrownBy(() -> handler.handle(new DeleteRoomCommand(RoomId.generate())))
                        .isInstanceOf(RoomNotFoundException.class)
                        .hasFieldOrPropertyWithValue("code", "ROOM_NOT_FOUND");
            }

            @Test
            @DisplayName("when the room is already removed")
            void whenTheRoomIsAlreadyRemoved() {
                handler.handle(new DeleteRoomCommand(dodoId));

                assertThatThrownBy(() -> handler.handle(new DeleteRoomCommand(dodoId)))
                        .isInstanceOf(RoomNotFoundException.class)
                        .hasFieldOrPropertyWithValue("code", "ROOM_NOT_FOUND");
            }
        }
    }
}
