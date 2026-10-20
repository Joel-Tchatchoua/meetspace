package com.accenture.meetspace.room.units;

import com.accenture.meetspace.room.application.usecases.update.UpdateSiteCommand;
import com.accenture.meetspace.room.application.usecases.update.UpdateSiteCommandHandler;
import com.accenture.meetspace.room.application.usecases.update.UpdateSiteResponse;
import com.accenture.meetspace.room.domain.entities.Site;
import com.accenture.meetspace.room.domain.exceptions.SiteCodeAlreadyUsedException;
import com.accenture.meetspace.room.domain.exceptions.SiteNameAlreadyUsedException;
import com.accenture.meetspace.room.domain.exceptions.SiteNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Update site")
class UpdateSiteTest {

    private static final SiteId SITE_ID = SiteId.generate();
    private static final ZoneId MAURITIUS = ZoneId.of("Indian/Mauritius");

    @Nested
    @DisplayName("command")
    class Command {

        @Nested
        @DisplayName("is valid")
        class Valid {

            @Test
            @DisplayName("with all its fields")
            void withAllItsFields() {
                var command = new UpdateSiteCommand(SITE_ID, "EBN", "Ebène", "Cybercity, Ebène", "Europe/Paris");

                assertThat(command.siteId()).isEqualTo(SITE_ID);
                assertThat(command.code()).isEqualTo("EBN");
                assertThat(command.name()).isEqualTo("Ebène");
                assertThat(command.address()).isEqualTo("Cybercity, Ebène");
                assertThat(command.timeZone()).isEqualTo("Europe/Paris");
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = "  ")
            @DisplayName("with Indian/Mauritius as default time zone")
            void withIndianMauritiusAsDefaultTimeZone(String timeZone) {
                var command = new UpdateSiteCommand(SITE_ID, "EBN", "Ebène", "Cybercity, Ebène", timeZone);

                assertThat(command.timeZone()).isEqualTo("Indian/Mauritius");
            }
        }

        @Nested
        @DisplayName("is rejected")
        class Rejected {

            @Test
            @DisplayName("without site id")
            void withoutSiteId() {
                assertThatThrownBy(() -> new UpdateSiteCommand(null, "EBN", "Ebène", "Cybercity, Ebène", null))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "siteId");
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = "  ")
            @DisplayName("without code")
            void withoutCode(String code) {
                assertThatThrownBy(() -> new UpdateSiteCommand(SITE_ID, code, "Ebène", "Cybercity, Ebène", null))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "code");
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = "  ")
            @DisplayName("without name")
            void withoutName(String name) {
                assertThatThrownBy(() -> new UpdateSiteCommand(SITE_ID, "EBN", name, "Cybercity, Ebène", null))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "name");
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = "  ")
            @DisplayName("without address")
            void withoutAddress(String address) {
                assertThatThrownBy(() -> new UpdateSiteCommand(SITE_ID, "EBN", "Ebène", address, null))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "address");
            }

            @Test
            @DisplayName("with an unknown time zone")
            void withAnUnknownTimeZone() {
                assertThatThrownBy(() -> new UpdateSiteCommand(SITE_ID, "EBN", "Ebène", "Cybercity, Ebène", "Mars/Olympus"))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "timeZone");
            }
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemorySiteRepository siteRepository = new InMemorySiteRepository();
        private final UpdateSiteCommandHandler handler = new UpdateSiteCommandHandler(siteRepository);

        private final SiteId ebeneId = SiteId.generate();
        private final SiteId portLouisId = SiteId.generate();

        @BeforeEach
        void givenTwoSites() {
            siteRepository.save(Site.create(ebeneId, "EBN", "Ebène", "Cybercity, Ebène", MAURITIUS));
            siteRepository.save(Site.create(portLouisId, "PLU", "Port-Louis", "Caudan", MAURITIUS));
        }

        @Nested
        @DisplayName("succeeds")
        class Succeeds {

            @Test
            @DisplayName("returning the updated site id")
            void returningTheUpdatedSiteId() {
                var response = handler.handle(new UpdateSiteCommand(ebeneId, "EBX", "Ebène Tower", "Tower 1", null));

                assertThat(response).isEqualTo(new UpdateSiteResponse(ebeneId.value(), true));
            }

            @Test
            @DisplayName("replacing every detail of the site")
            void replacingEveryDetailOfTheSite() {
                handler.handle(new UpdateSiteCommand(ebeneId, "EBX", "Ebène Tower", "Tower 1, Ebène", "Europe/Paris"));

                var site = siteRepository.findById(ebeneId).orElseThrow().snapshot();
                assertThat(site.code()).isEqualTo("EBX");
                assertThat(site.name()).isEqualTo("Ebène Tower");
                assertThat(site.address()).isEqualTo("Tower 1, Ebène");
                assertThat(site.timeZone()).isEqualTo(ZoneId.of("Europe/Paris"));
            }

            @Test
            @DisplayName("keeping its own code and name")
            void keepingItsOwnCodeAndName() {
                handler.handle(new UpdateSiteCommand(ebeneId, "EBN", "Ebène", "Tower 1, Ebène", null));

                var site = siteRepository.findById(ebeneId).orElseThrow().snapshot();
                assertThat(site.code()).isEqualTo("EBN");
                assertThat(site.name()).isEqualTo("Ebène");
                assertThat(site.address()).isEqualTo("Tower 1, Ebène");
            }
        }

        @Nested
        @DisplayName("fails")
        class Fails {

            @Test
            @DisplayName("when the site does not exist")
            void whenTheSiteDoesNotExist() {
                var unknown = new UpdateSiteCommand(SiteId.generate(), "NEW", "Nowhere", "Nowhere", null);

                assertThatThrownBy(() -> handler.handle(unknown))
                        .isInstanceOf(SiteNotFoundException.class)
                        .hasFieldOrPropertyWithValue("code", "SITE_NOT_FOUND");
            }

            @Test
            @DisplayName("when the site is deleted")
            void whenTheSiteIsDeleted() {
                siteRepository.findById(ebeneId).orElseThrow().delete();

                assertThatThrownBy(() -> handler.handle(new UpdateSiteCommand(ebeneId, "EBN", "Ebène", "Tower 1", null)))
                        .isInstanceOf(SiteNotFoundException.class)
                        .hasFieldOrPropertyWithValue("code", "SITE_NOT_FOUND");
            }

            @Test
            @DisplayName("when the code is used by another site")
            void whenTheCodeIsUsedByAnotherSite() {
                assertThatThrownBy(() -> handler.handle(new UpdateSiteCommand(ebeneId, "PLU", "Ebène", "Cybercity", null)))
                        .isInstanceOf(SiteCodeAlreadyUsedException.class)
                        .hasFieldOrPropertyWithValue("code", "SITE_CODE_ALREADY_USED");
                assertThat(siteRepository.findById(ebeneId).orElseThrow().snapshot().code()).isEqualTo("EBN");
            }

            @Test
            @DisplayName("when the name is used by another site")
            void whenTheNameIsUsedByAnotherSite() {
                assertThatThrownBy(() -> handler.handle(new UpdateSiteCommand(ebeneId, "EBN", "Port-Louis", "Cybercity", null)))
                        .isInstanceOf(SiteNameAlreadyUsedException.class)
                        .hasFieldOrPropertyWithValue("code", "SITE_NAME_ALREADY_USED");
                assertThat(siteRepository.findById(ebeneId).orElseThrow().snapshot().name()).isEqualTo("Ebène");
            }
        }
    }
}
