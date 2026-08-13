package com.quickqueue.global.auth.jwt;

import com.quickqueue.domain.member.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
                            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        // secret 으로 JWT 서명에 사용할 키 생성
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String createAccessToken(Member member) {
        return createToken(member, accessTokenExpiration, "access");
    }

    public String createRefreshToken(Member member) {
        return createToken(member, refreshTokenExpiration, "refresh");
    }

    // AccessToken, RefreshToken 의 공통 생성 로직
    private String createToken(Member member, long expiration, String tokenType) {
        Date now = new Date();
        Date expires = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim("role", member.getRole().name())
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(expires)
                .signWith(key)
                .compact();
    }

    // JWT 검증
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
