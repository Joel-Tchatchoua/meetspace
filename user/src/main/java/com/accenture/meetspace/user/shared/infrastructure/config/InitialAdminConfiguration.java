package com.accenture.meetspace.user.shared.infrastructure.config;

import com.accenture.meetspace.bootstrap.application.command.CommandBus;
import com.accenture.meetspace.user.profile.infrastructure.adapters.in.startup.InitialAdminRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty("meetspace.initial-admin.username")
@EnableConfigurationProperties(InitialAdminProperties.class)
public class InitialAdminConfiguration {

    @Bean
    InitialAdminRunner initialAdminRunner(CommandBus commandBus, InitialAdminProperties properties) {
        return new InitialAdminRunner(commandBus, properties.username());
    }
}
