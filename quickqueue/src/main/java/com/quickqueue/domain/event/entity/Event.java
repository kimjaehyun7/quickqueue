package com.quickqueue.domain.event.entity;

import com.quickqueue.domain.member.entity.Member;
import com.quickqueue.global.common.BaseCreatedTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class Event extends BaseCreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String name;

    // 서비스 식별자 (uuid 사용)
    @Column(name = "public_id", nullable = false, unique = true)
    private String publicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    private LocalDateTime closedAt;

    public static Event create() {
        return Event.builder().
                // TODO

                build();
    }

    public void close() {
        this.status = EventStatus.CLOSED;
        closedAt = LocalDateTime.now();
    }
}
