package com.d1ff.authservice.dto.response;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record AuthResponse(
        String accessToken,
        OffsetDateTime expiresAt
)
{}
