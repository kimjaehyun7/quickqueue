package com.quickqueue.domain.reservation.dto;

import com.quickqueue.domain.reservation.entity.ReservationStatus;

public record ReservationResponse(
        Long id,
        String representativeName,
        int peopleCount,
        int waitingNumber,
        long waitingAhead,
        String reservationToken,
        ReservationStatus status
) {
}
