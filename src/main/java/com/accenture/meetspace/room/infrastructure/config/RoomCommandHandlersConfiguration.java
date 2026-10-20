package com.accenture.meetspace.room.infrastructure.config;

import com.accenture.meetspace.room.application.usecases.create.CreateSiteCommand;
import com.accenture.meetspace.room.application.usecases.create.CreateSiteCommandHandler;
import com.accenture.meetspace.room.application.usecases.create.CreateSiteResponse;
import com.accenture.meetspace.room.application.port.out.repository.SiteRepository;
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
