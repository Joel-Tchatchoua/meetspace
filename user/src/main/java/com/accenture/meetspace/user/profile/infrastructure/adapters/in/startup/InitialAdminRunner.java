package com.accenture.meetspace.user.profile.infrastructure.adapters.in.startup;

import com.accenture.meetspace.bootstrap.application.command.CommandBus;
import com.accenture.meetspace.bootstrap.domain.DomainException;
import com.accenture.meetspace.user.profile.application.usecases.command.createinitialadmin.CreateInitialAdminCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public class InitialAdminRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitialAdminRunner.class);

    private final CommandBus commandBus;
    private final String username;

    public InitialAdminRunner(CommandBus commandBus, String username) {
        this.commandBus = commandBus;
        this.username = username;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        try {
            var response = commandBus.dispatch(new CreateInitialAdminCommand(username));
            if (response.created()) {
                LOGGER.info("Created the profile of the initial admin {}", username);
            }
        } catch (DomainException exception) {
            LOGGER.warn("Could not create the profile of the initial admin {}: {}", username,
                    exception.getMessage());
        }
    }
}
