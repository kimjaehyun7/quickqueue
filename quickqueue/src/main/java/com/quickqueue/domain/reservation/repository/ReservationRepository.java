package com.quickqueue.domain.reservation.repository;

import com.quickqueue.domain.reservation.entity.Reservation;
import com.quickqueue.domain.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationToken(String reservationToken);

    List<Reservation> findByEventIdAndStatusOrderByWaitingNumberAsc(Long eventId,
                                                                    ReservationStatus status);

    // 특정 사용자 앞에 대기자 수 반환
    long countByEventIdAndStatus(Long eventId, ReservationStatus status);

}
