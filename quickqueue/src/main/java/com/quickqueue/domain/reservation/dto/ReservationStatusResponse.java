package com.quickqueue.domain.reservation.dto;

import com.quickqueue.domain.reservation.entity.ReservationStatus;

public record ReservationStatusResponse(
        int waitingNumber,
        ReservationStatus status,
        long waitingAhead
) {
}
