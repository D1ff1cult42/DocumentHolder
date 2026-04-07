package com.d1ff.authservice.service.jwt.impl;

import com.d1ff.authservice.entity.User;
import com.d1ff.authservice.exceptions.TokenException;
import com.d1ff.authservice.service.jwt.interfaces.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    private final RedisTemplate<String, String> redisTemplate;

    private static final String TOKEN_PREFIX = "refresh-token:";
    private static final String USER_TOKEN_PREFIX = "user-token:";

    private static final RedisScript<Long> CREATE_TOKEN_SCRIPT = RedisScript.of("""
            redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[3])
            redis.call('SADD', KEYS[2], ARGV[2])
            return 1
            """, Long.class);

    private static final RedisScript<String> FIND_AND_REVOKE_SCRIPT = RedisScript.of("""
            local userId = redis.call('GET', KEYS[1])
            if not userId then return nil end
            redis.call('DEL', KEYS[1])
            redis.call('SREM', ARGV[2] .. userId, ARGV[1])
            return userId
            """, String.class);

    private static final RedisScript<Long> REVOKE_ALL_SCRIPT = RedisScript.of("""
            local tokens = redis.call('SMEMBERS', KEYS[1])
            for _, token in ipairs(tokens) do
                redis.call('DEL', ARGV[1] .. token)
            end
            redis.call('DEL', KEYS[1])
            return #tokens
            """, Long.class);

    @Override
    public void createRefreshToken(User user, HttpServletResponse response) {
        String token = UUID.randomUUID().toString();
        String accountId = user.getId().toString();

        redisTemplate.execute(
                CREATE_TOKEN_SCRIPT,
                List.of(TOKEN_PREFIX + token, USER_TOKEN_PREFIX + accountId),
                accountId,
                token,
                String.valueOf(refreshTokenExpiration.toSeconds())
        );

        log.info("Created refresh token for account {}", accountId);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(refreshTokenExpiration)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public String findAndRevokeToken(String token) {
        String userId = redisTemplate.execute(
                FIND_AND_REVOKE_SCRIPT,
                List.of(TOKEN_PREFIX + token),
                token,
                USER_TOKEN_PREFIX
        );
        if (userId == null) {
            throw new TokenException("Refresh token not found or expired");
        }
        log.info("Revoked refresh token for account {}", userId);
        return userId;
    }

    @Override
    public void revokeAllUserTokens(User user) {
        String userId = user.getId().toString();
        Long revoked = redisTemplate.execute(
                REVOKE_ALL_SCRIPT,
                List.of(USER_TOKEN_PREFIX + userId),
                TOKEN_PREFIX
        );
        log.info("Revoked {} tokens for user {}", revoked, userId);
    }
}
