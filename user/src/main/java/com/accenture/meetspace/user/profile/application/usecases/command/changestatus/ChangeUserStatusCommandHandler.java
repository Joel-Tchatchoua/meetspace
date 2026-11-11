package com.accenture.meetspace.user.profile.application.usecases.command.changestatus;

import com.accenture.meetspace.bootstrap.application.command.CommandHandler;
import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityProvider;
import com.accenture.meetspace.user.profile.application.port.out.repository.UserRepository;
import com.accenture.meetspace.user.profile.application.port.out.reservation.ReservationCanceller;
import com.accenture.meetspace.user.profile.domain.entities.User;
import com.accenture.meetspace.user.profile.domain.exceptions.UserNotFoundException;

public class ChangeUserStatusCommandHandler
        implements CommandHandler<ChangeUserStatusCommand, ChangeUserStatusResponse> {

    private final UserRepository userRepository;
    private final IdentityProvider identityProvider;
    private final ReservationCanceller reservationCanceller;

    public ChangeUserStatusCommandHandler(UserRepository userRepository, IdentityProvider identityProvider,
                                          ReservationCanceller reservationCanceller) {
        this.userRepository = userRepository;
        this.identityProvider = identityProvider;
        this.reservationCanceller = reservationCanceller;
    }

    @Override
    public ChangeUserStatusResponse handle(ChangeUserStatusCommand command) {
        var user = userRepository.findUser(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        if (command.active()) {
            activate(user);
        } else {
            deactivate(user, command);
        }
        return new ChangeUserStatusResponse(command.userId(), command.active());
    }

    private void activate(User user) {
        if (user.activate()) {
            userRepository.save(user);
            identityProvider.enableUser(user.snapshot().keycloakUserId());
        }
    }

    private void deactivate(User user, ChangeUserStatusCommand command) {
        if (user.deactivate(command.actorId())) {
            userRepository.save(user);
            identityProvider.disableUser(user.snapshot().keycloakUserId());
            reservationCanceller.cancelUpcomingReservations(command.userId());
        }
    }
}
