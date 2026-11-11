package com.accenture.meetspace.user.shared.infrastructure.config;

import com.accenture.meetspace.bootstrap.application.command.CommandHandlerRegistration;
import com.accenture.meetspace.user.auth.application.usecases.command.login.LoginCommand;
import com.accenture.meetspace.user.auth.application.usecases.command.login.LoginCommandHandler;
import com.accenture.meetspace.user.auth.application.usecases.command.login.LoginResponse;
import com.accenture.meetspace.user.auth.application.usecases.command.logout.LogoutCommand;
import com.accenture.meetspace.user.auth.application.usecases.command.logout.LogoutCommandHandler;
import com.accenture.meetspace.user.auth.application.usecases.command.logout.LogoutResponse;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommand;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeCommandHandler;
import com.accenture.meetspace.user.auth.application.usecases.command.register.RegisterEmployeeResponse;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityProvider;
import com.accenture.meetspace.user.profile.application.port.out.repository.UserRepository;
import com.accenture.meetspace.user.profile.application.port.out.reservation.ReservationCanceller;
import com.accenture.meetspace.user.profile.application.port.out.site.SiteCatalog;
import com.accenture.meetspace.user.profile.application.usecases.command.changerole.ChangeUserRoleCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.changerole.ChangeUserRoleCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.command.changerole.ChangeUserRoleResponse;
import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.command.changestatus.ChangeUserStatusResponse;
import com.accenture.meetspace.user.profile.application.usecases.command.createinitialadmin.CreateInitialAdminCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.createinitialadmin.CreateInitialAdminCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.command.createinitialadmin.CreateInitialAdminResponse;
import com.accenture.meetspace.user.profile.application.usecases.command.updateprofile.UpdateProfileCommand;
import com.accenture.meetspace.user.profile.application.usecases.command.updateprofile.UpdateProfileCommandHandler;
import com.accenture.meetspace.user.profile.application.usecases.command.updateprofile.UpdateProfileResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UserCommandHandlersConfiguration {

    @Bean
    CommandHandlerRegistration<RegisterEmployeeCommand, RegisterEmployeeResponse> registerEmployee(
            UserRepository userRepository, IdentityProvider identityProvider) {
        return new CommandHandlerRegistration<>(RegisterEmployeeCommand.class,
                new RegisterEmployeeCommandHandler(userRepository, identityProvider));
    }

    @Bean
    CommandHandlerRegistration<LoginCommand, LoginResponse> login(UserRepository userRepository,
                                                                  IdentityProvider identityProvider) {
        return new CommandHandlerRegistration<>(LoginCommand.class,
                new LoginCommandHandler(userRepository, identityProvider));
    }

    @Bean
    CommandHandlerRegistration<LogoutCommand, LogoutResponse> logout(IdentityProvider identityProvider) {
        return new CommandHandlerRegistration<>(LogoutCommand.class, new LogoutCommandHandler(identityProvider));
    }


    @Bean
    CommandHandlerRegistration<CreateInitialAdminCommand, CreateInitialAdminResponse> createInitialAdmin(
            UserRepository userRepository, IdentityProvider identityProvider) {
        return new CommandHandlerRegistration<>(CreateInitialAdminCommand.class,
                new CreateInitialAdminCommandHandler(userRepository, identityProvider));
    }

    @Bean
    CommandHandlerRegistration<UpdateProfileCommand, UpdateProfileResponse> updateProfile(
            UserRepository userRepository, SiteCatalog siteCatalog) {
        return new CommandHandlerRegistration<>(UpdateProfileCommand.class,
                new UpdateProfileCommandHandler(userRepository, siteCatalog));
    }

    @Bean
    CommandHandlerRegistration<ChangeUserRoleCommand, ChangeUserRoleResponse> changeUserRole(
            UserRepository userRepository, IdentityProvider identityProvider) {
        return new CommandHandlerRegistration<>(ChangeUserRoleCommand.class,
                new ChangeUserRoleCommandHandler(userRepository, identityProvider));
    }

    @Bean
    CommandHandlerRegistration<ChangeUserStatusCommand, ChangeUserStatusResponse> changeUserStatus(
            UserRepository userRepository, IdentityProvider identityProvider,
            ReservationCanceller reservationCanceller) {
        return new CommandHandlerRegistration<>(ChangeUserStatusCommand.class,
                new ChangeUserStatusCommandHandler(userRepository, identityProvider, reservationCanceller));
    }
}
