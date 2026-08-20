package com.quickqueue.domain.reservation.dto;

import com.quickqueue.domain.reservation.entity.ReservationStatus;

public record ReservationResponse(
        Long id,
        String representativeName,
        int peopleCount,
        int waitingNumber,
        String reservationToken,
        ReservationStatus status
) {
}
