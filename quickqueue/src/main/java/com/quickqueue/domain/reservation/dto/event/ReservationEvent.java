package com.quickqueue.domain.reservation.dto.event;

import com.quickqueue.domain.reservation.dto.ReservationStatusResponse;

public record ReservationEvent(
        String reservationToken,
        String type,
        ReservationStatusResponse response
) {
}
