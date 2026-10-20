package com.accenture.meetspace.room.infrastructure.config;

import com.accenture.meetspace.room.application.command.create.CreateSiteCommand;
import com.accenture.meetspace.room.application.command.create.CreateSiteCommandHandler;
import com.accenture.meetspace.room.application.command.create.CreateSiteResponse;
import com.accenture.meetspace.room.application.port.out.SiteRepository;
import com.accenture.meetspace.shared.application.command.CommandHandlerRegistration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RoomCommandHandlersConfiguration {

    @Bean
    CommandHandlerRegistration<CreateSiteCommand, CreateSiteResponse> createSite(SiteRepository siteRepository) {
        return new CommandHandlerRegistration<>(CreateSiteCommand.class, new CreateSiteCommandHandler(siteRepository));
    }
}
