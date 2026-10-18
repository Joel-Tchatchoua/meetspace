package com.accenture.meetspace.room.units;

import com.accenture.meetspace.room.application.command.create.CreateSiteCommand;
import com.accenture.meetspace.room.application.command.create.CreateSiteCommandHandler;
import com.accenture.meetspace.room.domain.exceptions.SiteCodeAlreadyUsedException;
import com.accenture.meetspace.room.domain.exceptions.SiteNameAlreadyUsedException;
import com.accenture.meetspace.room.domain.vo.SiteId;
import com.accenture.meetspace.room.units.stubs.InMemorySiteRepository;
import com.accenture.meetspace.shared.application.command.InvalidCommandException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Create site")
class CreateSiteTest {

    private static final SiteId SITE_ID = SiteId.generate();

    @Nested
    @DisplayName("command")
    class Command {

        @Nested
        @DisplayName("is valid")
        class Valid {

            @Test
            @DisplayName("with all its fields")
            void withAllItsFields() {
                var command = new CreateSiteCommand(SITE_ID, "EBN", "Ebène", "Cybercity, Ebène", "Europe/Paris");

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
                var command = new CreateSiteCommand(SITE_ID, "EBN", "Ebène", "Cybercity, Ebène", timeZone);

                assertThat(command.timeZone()).isEqualTo("Indian/Mauritius");
            }
        }

        @Nested
        @DisplayName("is rejected")
        class Rejected {

            @Test
            @DisplayName("without site id")
            void withoutSiteId() {
                assertThatThrownBy(() -> new CreateSiteCommand(null, "EBN", "Ebène", "Cybercity, Ebène", null))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "siteId");
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = "  ")
            @DisplayName("without code")
            void withoutCode(String code) {
                assertThatThrownBy(() -> new CreateSiteCommand(SITE_ID, code, "Ebène", "Cybercity, Ebène", null))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "code");
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = "  ")
            @DisplayName("without name")
            void withoutName(String name) {
                assertThatThrownBy(() -> new CreateSiteCommand(SITE_ID, "EBN", name, "Cybercity, Ebène", null))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "name");
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = "  ")
            @DisplayName("without address")
            void withoutAddress(String address) {
                assertThatThrownBy(() -> new CreateSiteCommand(SITE_ID, "EBN", "Ebène", address, null))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "address");
            }

            @Test
            @DisplayName("with an unknown time zone")
            void withAnUnknownTimeZone() {
                assertThatThrownBy(() -> new CreateSiteCommand(SITE_ID, "EBN", "Ebène", "Cybercity, Ebène", "Mars/Olympus"))
                        .isInstanceOf(InvalidCommandException.class)
                        .hasFieldOrPropertyWithValue("field", "timeZone");
            }
        }
    }

    @Nested
    @DisplayName("handler")
    class Handler {

        private final InMemorySiteRepository siteRepository = new InMemorySiteRepository();
        private final CreateSiteCommandHandler handler = new CreateSiteCommandHandler(siteRepository);

        private CreateSiteCommand ebene(SiteId siteId) {
            return new CreateSiteCommand(siteId, "EBN", "Ebène", "Cybercity, Ebène", "Indian/Mauritius");
        }

        @Nested
        @DisplayName("succeeds")
        class Succeeds {

            @Test
            @DisplayName("creating the site with the command details")
            void creatingTheSiteWithTheCommandDetails() {
                var siteId = SiteId.generate();

                handler.handle(ebene(siteId));

                var site = siteRepository.findById(siteId).orElseThrow().snapshot();
                assertThat(site.code()).isEqualTo("EBN");
                assertThat(site.name()).isEqualTo("Ebène");
                assertThat(site.address()).isEqualTo("Cybercity, Ebène");
                assertThat(site.timeZone()).isEqualTo(ZoneId.of("Indian/Mauritius"));
                assertThat(site.deleted()).isFalse();
            }
        }

        @Nested
        @DisplayName("fails")
        class Fails {

            @Test
            @DisplayName("when the code is already used by another site")
            void whenTheCodeIsAlreadyUsedByAnotherSite() {
                handler.handle(ebene(SiteId.generate()));
                var sameCode = new CreateSiteCommand(SiteId.generate(), "EBN", "Port-Louis", "Caudan", null);

                assertThatThrownBy(() -> handler.handle(sameCode))
                        .isInstanceOf(SiteCodeAlreadyUsedException.class)
                        .hasFieldOrPropertyWithValue("code", "SITE_CODE_ALREADY_USED");
                assertThat(siteRepository.count()).isEqualTo(1);
            }

            @Test
            @DisplayName("when the name is already used by another site")
            void whenTheNameIsAlreadyUsedByAnotherSite() {
                handler.handle(ebene(SiteId.generate()));
                var sameName = new CreateSiteCommand(SiteId.generate(), "PLU", "Ebène", "Caudan", null);

                assertThatThrownBy(() -> handler.handle(sameName))
                        .isInstanceOf(SiteNameAlreadyUsedException.class)
                        .hasFieldOrPropertyWithValue("code", "SITE_NAME_ALREADY_USED");
                assertThat(siteRepository.count()).isEqualTo(1);
            }
        }
    }
}
