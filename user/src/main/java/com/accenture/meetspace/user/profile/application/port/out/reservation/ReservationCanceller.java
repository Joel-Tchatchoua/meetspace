package com.accenture.meetspace.user.profile.application.port.out.reservation;

import java.util.UUID;

public interface ReservationCanceller {

    void cancelUpcomingReservations(UUID userId);
}
