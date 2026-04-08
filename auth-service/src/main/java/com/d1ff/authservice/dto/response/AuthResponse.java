package com.d1ff.authservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.OffsetDateTime;

@Schema(name = "AuthResponse", description = "Response containing authentication tokens")
@Builder
public record AuthResponse(
        @Schema(description = "Access token for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Expiration time of the access token", example = "2026-04-08T23:20:00.087Z")
        OffsetDateTime expiresAt
)
{}
