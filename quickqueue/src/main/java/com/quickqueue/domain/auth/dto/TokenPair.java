package com.quickqueue.domain.auth.dto;

public record TokenPair(
        String accessToken,
        String refreshToken
) {
}
