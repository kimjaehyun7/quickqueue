package com.quickqueue.domain.reservation.dto;

public record ReservationRequest(
        String representativeName,
        int peopleCount,
        String phoneNumber
) {
}
