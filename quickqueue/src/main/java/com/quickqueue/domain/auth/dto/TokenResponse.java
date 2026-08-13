package com.quickqueue.domain.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
