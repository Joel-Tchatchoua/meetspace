package com.accenture.meetspace.room.units;

import com.accenture.meetspace.room.application.usecases.delete.DeleteSiteCommand;
import com.accenture.meetspace.room.application.usecases.delete.DeleteSiteCommandHandler;
import com.accenture.meetspace.room.application.usecases.delete.DeleteSiteResponse;
import com.accenture.meetspace.room.domain.entities.Site;
import com.accenture.meetspace.room.domain.exceptions.SiteHasRoomsException;
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

import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Delete site")
class DeleteSiteTest {

    @Nested
    @DisplayName("command")
    class Command {

        @Test
        @DisplayName("is valid with a site id")
        void isValidWithASiteId() {
            var siteId = SiteId.generate();

            assertThat(new DeleteSiteCommand(siteId).siteId()).isEqualTo(siteId);
        }

        @Test
        @DisplayName("is rejected without site id")
        void isRejectedWithoutSiteId() {
            assertThatThrownBy(() -> new DeleteSiteCommand(null))
                    .isInstanceOf(InvalidCommandException.class)
                    .hasFieldOrPropertyWithValue("field", "siteId");
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemorySiteRepository siteRepository = new InMemorySiteRepository();
        private final DeleteSiteCommandHandler handler = new DeleteSiteCommandHandler(siteRepository);

        private final SiteId ebeneId = SiteId.generate();

        @BeforeEach
        void givenASite() {
            siteRepository.save(Site.create(ebeneId, "EBN", "Ebène", "Cybercity, Ebène", ZoneId.of("Indian/Mauritius")));
        }

        private Site ebene() {
            return siteRepository.findById(ebeneId).orElseThrow();
        }

        private RoomId addRoom() {
            var roomId = RoomId.generate();
            ebene().addRoom(roomId, "R101", "Dodo", 1, new Capacity(8), Set.of());
            return roomId;
        }

        @Nested
        @DisplayName("succeeds")
        class Succeeds {

            @Test
            @DisplayName("returning the deleted site id")
            void returningTheDeletedSiteId() {
                var response = handler.handle(new DeleteSiteCommand(ebeneId));

                assertThat(response).isEqualTo(new DeleteSiteResponse(ebeneId.value(), true));
            }

            @Test
            @DisplayName("soft-deleting a site without rooms")
            void softDeletingASiteWithoutRooms() {
                handler.handle(new DeleteSiteCommand(ebeneId));

                assertThat(siteRepository.findById(ebeneId)).isEmpty();
                assertThat(siteRepository.storedSite(ebeneId))
                        .hasValueSatisfying(site -> assertThat(site.snapshot().deleted()).isTrue());
            }

            @Test
            @DisplayName("soft-deleting a site whose rooms are all removed")
            void softDeletingASiteWhoseRoomsAreAllRemoved() {
                ebene().removeRoom(addRoom());

                handler.handle(new DeleteSiteCommand(ebeneId));

                assertThat(siteRepository.findById(ebeneId)).isEmpty();
            }
        }

        @Nested
        @DisplayName("fails")
        class Fails {

            @Test
            @DisplayName("when the site does not exist")
            void whenTheSiteDoesNotExist() {
                assertThatThrownBy(() -> handler.handle(new DeleteSiteCommand(SiteId.generate())))
                        .isInstanceOf(SiteNotFoundException.class)
                        .hasFieldOrPropertyWithValue("code", "SITE_NOT_FOUND");
            }

            @Test
            @DisplayName("when the site is already deleted")
            void whenTheSiteIsAlreadyDeleted() {
                handler.handle(new DeleteSiteCommand(ebeneId));

                assertThatThrownBy(() -> handler.handle(new DeleteSiteCommand(ebeneId)))
                        .isInstanceOf(SiteNotFoundException.class)
                        .hasFieldOrPropertyWithValue("code", "SITE_NOT_FOUND");
            }

            @Test
            @DisplayName("when the site still has rooms")
            void whenTheSiteStillHasRooms() {
                addRoom();

                assertThatThrownBy(() -> handler.handle(new DeleteSiteCommand(ebeneId)))
                        .isInstanceOf(SiteHasRoomsException.class)
                        .hasFieldOrPropertyWithValue("code", "SITE_HAS_ROOMS");
                assertThat(siteRepository.findById(ebeneId)).isPresent();
            }
        }
    }
}
