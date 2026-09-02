package com.quickqueue.domain.reservation.service;

import com.quickqueue.domain.reservation.dto.ReservationResponse;
import com.quickqueue.domain.reservation.dto.ReservationStatusResponse;
import com.quickqueue.domain.reservation.dto.event.ReservationEvent;
import com.quickqueue.domain.reservation.dto.event.ReservationListUpdateEvent;
import com.quickqueue.domain.reservation.dto.event.ReservationQueueUpdateEvent;
import com.quickqueue.domain.reservation.entity.Reservation;
import com.quickqueue.domain.reservation.entity.ReservationStatus;
import com.quickqueue.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final SseEmitters sseEmitters;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    // 관리자용
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleListUpdate(ReservationListUpdateEvent event) {
        List<ReservationResponse> reservations = reservationService.getReservations(
                event.memberId(),
                event.publicId()
        );

        sseEmitters.send(event.publicId(), "list", reservations);
    }

    // 사용자용
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleQueueUpdate(ReservationQueueUpdateEvent event) {
        List<Reservation> reservations = reservationRepository
                .findByEventIdAndStatusOrderByWaitingNumberAsc(
                        event.eventId(), ReservationStatus.WAITING
                );

        for (int i = 0; i < reservations.size(); i++) {
            Reservation r = reservations.get(i);

            ReservationStatusResponse response = new ReservationStatusResponse(
                    r.getWaitingNumber(),
                    r.getStatus(),
                    i
            );
            sseEmitters.send(r.getReservationToken(), "queue", response);
        }
    }

    // 사용자용
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCallCancelComplete(ReservationEvent event) {
        if (event.type().equals("called")) {
            sseEmitters.send(
                    event.reservationToken(),
                    event.type(),
                    event.response()
            );
        } else if (event.type().equals("canceled")) {
            sseEmitters.send(
                    event.reservationToken(),
                    event.type(),
                    event.response()
            );
            sseEmitters.complete(event.reservationToken());
        } else { // completed
            sseEmitters.complete(event.reservationToken());
        }
    }
}
