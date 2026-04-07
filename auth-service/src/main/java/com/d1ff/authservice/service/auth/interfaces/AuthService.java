package com.d1ff.authservice.service.auth.interfaces;

import com.d1ff.authservice.dto.request.LoginRequest;
import com.d1ff.authservice.dto.request.RegisterRequest;
import com.d1ff.authservice.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    void register(String ip, String userAgent, RegisterRequest request, HttpServletResponse response);

    AuthResponse login(String ip, String userAgent, LoginRequest request, HttpServletResponse response);

    AuthResponse refreshToken(String ip, String userAgent, String refreshToken, HttpServletResponse response);

    void logout(String ip, String userAgent, String refreshToken);

    void logoutAll(String ip, String userAgent, String email);
}
