package com.accenture.meetspace.user.profile.infrastructure.adapters.out.reservation;

import com.accenture.meetspace.user.profile.application.port.out.reservation.ReservationCanceller;

import java.util.UUID;

public class NoReservationCanceller implements ReservationCanceller {

    @Override
    public void cancelUpcomingReservations(UUID userId) {
    }
}
