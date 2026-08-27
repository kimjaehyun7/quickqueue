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
    private final SseEmitters sseEmitters;

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

        long waitingAhead = getWaitingAhead(reservation);

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

        ReservationStatusResponse response = new ReservationStatusResponse(reservation.getWaitingNumber(),
                reservation.getStatus(), 0);

        sseEmitters.send(reservationToken, "called", response);
        // TODO : 예약 대표자에게 문자 발송
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

        if (reservation.getStatus() != ReservationStatus.CALLED) {
            throw new RuntimeException("호출된 예약만 완료할 수 있습니다.");
        }

        // 완료
        reservation.complete();

        // 대기열 업데이트 sse 전송
        queueUpdate(event.getId());

        sseEmitters.complete(reservationToken);
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

        // 대기열 업데이트 sse 전송
        queueUpdate(event.getId());

        ReservationStatusResponse response = new ReservationStatusResponse(reservation.getWaitingNumber(),
                reservation.getStatus(), 0);

        sseEmitters.send(reservationToken, "canceled", response);

        // 취소 sse 전송 후 연결 종료
        sseEmitters.complete(reservationToken);
    }

    private String makeReservationToken() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);
    }

    // 대기자 수 반환
    private long getWaitingAhead(Reservation reservation) {
        return reservationRepository.findByEventIdAndStatusOrderByWaitingNumberAsc(
                        reservation.getEvent().getId(),
                        ReservationStatus.WAITING
                )
                .stream()
                .filter(r -> r.getWaitingNumber() < reservation.getWaitingNumber())
                .count();
    }

    // 대기열 업데이트
    // 반복문을 통해서 카운트 쿼리를 대기자수 만큼이 아닌 1번만 동작하게 최적화.
    private void queueUpdate(Long eventId) {

        List<Reservation> reservations = reservationRepository
                .findByEventIdAndStatusOrderByWaitingNumberAsc(
                        eventId, ReservationStatus.WAITING
                );

        for (int i = 0; i < reservations.size(); i++) {

            Reservation r = reservations.get(i);

            ReservationStatusResponse response = new ReservationStatusResponse(
                    r.getWaitingNumber(),
                    r.getStatus(),
                    i
            );

            sseEmitters.send(r.getReservationToken(), "waiting", response);
        }
    }
}
