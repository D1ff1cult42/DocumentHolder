package com.d1ff.authservice.service.jwt.interfaces;

import com.d1ff.authservice.entity.User;
import jakarta.servlet.http.HttpServletResponse;

public interface RefreshTokenService {
    void createRefreshToken(User user, HttpServletResponse response);

    String findAndRevokeToken(String token);

    void revokeAllUserTokens(User user);
}

