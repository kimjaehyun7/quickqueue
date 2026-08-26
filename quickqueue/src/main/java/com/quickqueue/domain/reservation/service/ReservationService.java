package com.quickqueue.domain.reservation.service;

import com.quickqueue.domain.event.entity.Event;
import com.quickqueue.domain.event.entity.EventStatus;
import com.quickqueue.domain.event.repository.EventRepository;
import com.quickqueue.domain.member.entity.Member;
import com.quickqueue.domain.member.repository.MemberRepository;
import com.quickqueue.domain.reservation.dto.ReservationRequest;
import com.quickqueue.domain.reservation.dto.ReservationResponse;
import com.quickqueue.domain.reservation.dto.ReservationStatusResponse;
import com.quickqueue.domain.reservation.entity.Reservation;
import com.quickqueue.domain.reservation.entity.ReservationStatus;
import com.quickqueue.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public ReservationResponse createReservation(String publicId, ReservationRequest request) {

        // event 조회
        Event event = eventRepository.findByPublicIdUseLock(publicId)
                .orElseThrow(
                        // TODO
                );

        // 예약 가능 여부 확인
        if (event.getStatus() != EventStatus.OPEN) {
            throw new IllegalStateException("현재 예약을 받고 있지 않습니다.");
        }

        // 현재 대기자 수 조회
        int waitingNumber = event.issueWaitingNumber();

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

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservations(Long memberId, String publicId) {

        // event 조회
        Event event = eventRepository.findByPublicId(publicId)
                .orElseThrow(
                        // TODO
                );

        // 관리자 체크
        if (!event.isOwner(memberId)) {
            throw new RuntimeException("권한이 없습니다.");
            // TODO
        }

        return reservationRepository
                .findByEventIdOrderByWaitingNumberAsc(event.getId())
                .stream()
                .map(r -> new ReservationResponse(
                        r.getId(),
                        r.getRepresentativeName(),
                        r.getPeopleCount(),
                        r.getWaitingNumber(),
                        r.getReservationToken(),
                        r.getStatus()
                )).toList();
    }

    public void callReservation(Long memberId, String publicId, String reservationToken) {

        Event event = eventRepository.findByPublicId(publicId)
                .orElseThrow(
                        // TODO
                );

        // 관리자 체크
        if (!event.isOwner(memberId)) {
            throw new RuntimeException("권한이 없습니다.");
            // TODO
        }

        Reservation reservation = reservationRepository.findByReservationToken(reservationToken)
                .orElseThrow(
                        // TODO
                );

        // 해당 예약이 이 이벤트의 예약인지 체크
        if (!reservation.getEvent().getId().equals(event.getId())) {
            throw new RuntimeException("해당 이벤트의 예약이 아닙니다.");
            // TODO
        }

        if (reservation.getStatus() != ReservationStatus.WAITING) {
            throw new RuntimeException("대기 중인 예약만 호출할 수 있습니다.");
        }

        // 호출
        reservation.call();
        // TODO : 예약 대표자에게 문자 / 카카오톡 발송
    }

    public void completeReservation(Long memberId, String publicId, String reservationToken) {

        Event event = eventRepository.findByPublicId(publicId)
                .orElseThrow(
                        // TODO
                );

        // 관리자 체크
        if (!event.isOwner(memberId)) {
            throw new RuntimeException("권한이 없습니다.");
            // TODO
        }

        Reservation reservation = reservationRepository.findByReservationToken(reservationToken)
                .orElseThrow(
                        // TODO
                );

        // 해당 예약이 이 이벤트의 예약인지 체크
        if (!reservation.getEvent().getId().equals(event.getId())) {
            throw new RuntimeException("해당 이벤트의 예약이 아닙니다.");
            // TODO
        }

        if (reservation.getStatus() != ReservationStatus.WAITING) {
            throw new RuntimeException("대기 중인 예약만 호출할 수 있습니다.");
        }

        // 완료
        reservation.complete();
    }

    public void cancelReservation(Long memberId, String publicId, String reservationToken) {
        Event event = eventRepository.findByPublicId(publicId)
                .orElseThrow(
                        // TODO
                );

        // 관리자 체크
        if (!event.isOwner(memberId)) {
            throw new RuntimeException("권한이 없습니다.");
            // TODO
        }

        Reservation reservation = reservationRepository.findByReservationToken(reservationToken)
                .orElseThrow(
                        // TODO
                );

        // 해당 예약이 이 이벤트의 예약인지 체크
        if (!reservation.getEvent().getId().equals(event.getId())) {
            throw new RuntimeException("해당 이벤트의 예약이 아닙니다.");
            // TODO
        }

        reservation.cancel();
    }

    private String makeReservationToken() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);
    }


}
