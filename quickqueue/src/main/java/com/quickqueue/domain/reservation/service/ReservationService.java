package com.quickqueue.domain.reservation.service;

import com.quickqueue.domain.event.entity.Event;
import com.quickqueue.domain.event.entity.EventStatus;
import com.quickqueue.domain.event.repository.EventRepository;
import com.quickqueue.domain.reservation.dto.ReservationRequest;
import com.quickqueue.domain.reservation.dto.ReservationResponse;
import com.quickqueue.domain.reservation.dto.ReservationStatusResponse;
import com.quickqueue.domain.reservation.entity.Reservation;
import com.quickqueue.domain.reservation.entity.ReservationStatus;
import com.quickqueue.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;

    public ReservationResponse createReservation(String publicId, ReservationRequest request) {

        // event 조회
        Event event = eventRepository.findByPublicId(publicId)
                .orElseThrow(
                        // TODO
                );

        // 예약 가능 여부 확인
        if (event.getStatus() != EventStatus.OPEN) {
            throw new IllegalStateException("현재 예약을 받고 있지 않습니다.");
        }

        // 현재 대기자 수 조회
        int waitingNumber =
                (int) reservationRepository
                        .countByEventIdAndStatus(
                                event.getId(),
                                ReservationStatus.WAITING
                        ) + 1;

        // 예약 코드 생성
        String reservationToken = makeReservationToken();

        // 예약 생성
        Reservation reservation = Reservation.create(
                event,
                reservationToken,
                request,
                waitingNumber
        );

        reservationRepository.save(reservation);

        return new ReservationResponse(
                reservation.getId(),
                reservation.getRepresentativeName(),
                reservation.getPeopleCount(),
                reservation.getWaitingNumber(),
                reservation.getReservationToken(),
                reservation.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public ReservationStatusResponse getReservation(String reservationToken) {
        Reservation reservation = reservationRepository.findByReservationToken(reservationToken)
                .orElseThrow(
                        // TODO
                );

        long waitingAhead = reservationRepository.findByEventIdAndStatusOrderByWaitingNumberAsc(
                        reservation.getEvent().getId(),
                        ReservationStatus.WAITING
                )
                .stream()
                .filter(r -> r.getWaitingNumber() < reservation.getWaitingNumber())
                .count();

        return new ReservationStatusResponse(
                reservation.getWaitingNumber(),
                reservation.getStatus(),
                waitingAhead
        );
    }

    private String makeReservationToken() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);
    }


}
