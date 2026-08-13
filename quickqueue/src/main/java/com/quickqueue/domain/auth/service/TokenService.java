package com.quickqueue.domain.auth.service;

import com.quickqueue.domain.auth.dto.TokenResponse;
import com.quickqueue.domain.auth.entity.RefreshToken;
import com.quickqueue.domain.auth.repository.RefreshTokenRepository;
import com.quickqueue.domain.member.entity.Member;
import com.quickqueue.global.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // Access Token, Refresh Token 발급
    public TokenResponse issueToken(Member member) {

        String accessToken =
                jwtTokenProvider.createAccessToken(member);

        String refreshToken =
                jwtTokenProvider.createRefreshToken(member);

        // 기존 refresh token 이 있다면 삭제
        refreshTokenRepository.deleteByMemberId(member.getId());

        // 새로운 refresh token 을 db에 저장
        RefreshToken token = RefreshToken.create(
                member,
                refreshToken,
                LocalDateTime.now()
                        .plusSeconds(refreshTokenExpiration / 1000));

        refreshTokenRepository.save(token);

        return new TokenResponse(accessToken, refreshToken);
    }
}
