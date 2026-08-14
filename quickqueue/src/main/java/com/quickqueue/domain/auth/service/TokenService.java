package com.quickqueue.domain.auth.service;

import com.quickqueue.domain.auth.dto.AccessTokenResponse;
import com.quickqueue.domain.auth.dto.TokenPair;
import com.quickqueue.domain.auth.entity.RefreshToken;
import com.quickqueue.domain.auth.repository.RefreshTokenRepository;
import com.quickqueue.domain.member.entity.Member;
import com.quickqueue.global.auth.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
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
    public TokenPair issueToken(Member member) {

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

        return new TokenPair(accessToken, refreshToken);
    }

    // Refresh Token 으로 Access Token 재발급
    @Transactional(readOnly = true)
    public String reissueAccessToken(String refreshToken) {
        // jwt 검증
        Claims claims = jwtTokenProvider.parseClaims(refreshToken);

        // refresh token 인지 확인
        String tokenType = claims.get("type", String.class);

        if (!"refresh".equals(tokenType)) {
            // TODO
            throw new IllegalArgumentException("Refresh Token 이 아닙니다.");
        }

        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 Refresh Token 입니다."));

        if (savedToken.isExpired()) {
            throw new IllegalArgumentException("Refresh Token 이 만료되었습니다.");
        }

        Member member = savedToken.getMember();

        // access token 발급
        return jwtTokenProvider.createAccessToken(member);
    }

    // 로그아웃
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }
}
