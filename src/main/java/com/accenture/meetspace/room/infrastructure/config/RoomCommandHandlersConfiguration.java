package com.accenture.meetspace.room.infrastructure.config;

import com.accenture.meetspace.room.application.port.out.repository.SiteRepository;
import com.accenture.meetspace.room.application.usecases.command.create.CreateSiteCommand;
import com.accenture.meetspace.room.application.usecases.command.create.CreateSiteCommandHandler;
import com.accenture.meetspace.room.application.usecases.command.create.CreateSiteResponse;
import com.accenture.meetspace.room.application.usecases.command.delete.DeleteSiteCommand;
import com.accenture.meetspace.room.application.usecases.command.delete.DeleteSiteCommandHandler;
import com.accenture.meetspace.room.application.usecases.command.delete.DeleteSiteResponse;
import com.accenture.meetspace.room.application.usecases.command.update.UpdateSiteCommand;
import com.accenture.meetspace.room.application.usecases.command.update.UpdateSiteCommandHandler;
import com.accenture.meetspace.room.application.usecases.command.update.UpdateSiteResponse;
import com.accenture.meetspace.shared.application.command.CommandHandlerRegistration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RoomCommandHandlersConfiguration {

    @Bean
    CommandHandlerRegistration<CreateSiteCommand, CreateSiteResponse> createSite(SiteRepository siteRepository) {
        return new CommandHandlerRegistration<>(CreateSiteCommand.class, new CreateSiteCommandHandler(siteRepository));
    }

    @Bean
    CommandHandlerRegistration<UpdateSiteCommand, UpdateSiteResponse> updateSite(SiteRepository siteRepository) {
        return new CommandHandlerRegistration<>(UpdateSiteCommand.class, new UpdateSiteCommandHandler(siteRepository));
    }

    @Bean
    CommandHandlerRegistration<DeleteSiteCommand, DeleteSiteResponse> deleteSite(SiteRepository siteRepository) {
        return new CommandHandlerRegistration<>(DeleteSiteCommand.class, new DeleteSiteCommandHandler(siteRepository));
    }
}
