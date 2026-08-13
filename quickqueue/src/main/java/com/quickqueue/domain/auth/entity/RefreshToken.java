package com.quickqueue.domain.auth.entity;

import com.quickqueue.domain.member.entity.Member;
import com.quickqueue.global.common.BaseCreatedTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class RefreshToken extends BaseCreatedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public static RefreshToken create(Member member, String token, LocalDateTime expiresAt) {
        return RefreshToken.builder()
                .member(member)
                .token(token)
                .expiresAt(expiresAt)
                .build();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
