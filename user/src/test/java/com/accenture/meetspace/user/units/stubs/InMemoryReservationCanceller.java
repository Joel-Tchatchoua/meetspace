package com.accenture.meetspace.user.units.stubs;

import com.accenture.meetspace.user.profile.application.port.out.reservation.ReservationCanceller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class InMemoryReservationCanceller implements ReservationCanceller {

    private final List<UUID> cancelledFor = new ArrayList<>();

    @Override
    public void cancelUpcomingReservations(UUID userId) {
        cancelledFor.add(userId);
    }

    public List<UUID> cancelledFor() {
        return List.copyOf(cancelledFor);
    }
}
