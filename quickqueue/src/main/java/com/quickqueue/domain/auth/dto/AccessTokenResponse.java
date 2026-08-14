package com.quickqueue.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessTokenResponse(
        @JsonProperty("accessToken")
        String accessToken
) {
}
