package com.quickqueue.domain.reservation.dto.event;


public record ReservationListUpdateEvent(
        Long memberId,
        String publicId
) {
}
