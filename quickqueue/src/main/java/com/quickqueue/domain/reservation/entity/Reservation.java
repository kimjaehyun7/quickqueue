package com.quickqueue.domain.reservation.entity;

import com.quickqueue.domain.event.entity.Event;
import com.quickqueue.domain.reservation.dto.ReservationRequest;
import com.quickqueue.global.common.BaseCreatedTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class Reservation extends BaseCreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id",nullable = false)
    private Event event;

    // 예약 식별자 (uuid 사용)
    @Column(name = "reservation_token", nullable = false, unique = true)
    private String reservationToken;

    @Column(nullable = false)
    private String representativeName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private Integer peopleCount;

    @Column(nullable = false)
    private Integer waitingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.WAITING;

    private LocalDateTime calledAt;

    private LocalDateTime completedAt;

    public static Reservation create(Event event, String reservationToken,
                                     ReservationRequest request, Integer waitingNumber) {
        return Reservation.builder()
                .event(event)
                .reservationToken(reservationToken)
                .representativeName(request.representativeName())
                .phoneNumber(request.phoneNumber())
                .peopleCount(request.peopleCount())
                .waitingNumber(waitingNumber)
                .build();
    }

    public void call() {
        this.status = ReservationStatus.CALLED;
        this.calledAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = ReservationStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELED;
    }

}
