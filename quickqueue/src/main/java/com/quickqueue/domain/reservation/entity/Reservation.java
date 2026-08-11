package com.quickqueue.domain.reservation.entity;

import com.quickqueue.domain.event.entity.Event;
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

    private String phoneNumber;

    private Integer peopleCount;

    private Integer waitingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.WAITING;

    private LocalDateTime calledAt;

    private LocalDateTime completedAt;

    public static Reservation create() {
        return Reservation.builder().
                // TODO

                build();
    }

    public void call() {
        this.status = ReservationStatus.CALLED;
        this.calledAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = ReservationStatus.COMPLETED;
        this.calledAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELED;
        this.calledAt = LocalDateTime.now();
    }

}
