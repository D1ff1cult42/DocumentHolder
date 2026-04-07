package com.d1ff.authservice.dto.response;

import java.time.OffsetDateTime;

public record AccessTokenResponse(
        String accessToken,
        OffsetDateTime expiresAt
)
{}
