package com.d1ff.authservice.service.auth.impl;

import com.d1ff.authservice.dto.request.LoginRequest;
import com.d1ff.authservice.dto.request.RegisterRequest;
import com.d1ff.authservice.dto.response.AccessTokenResponse;
import com.d1ff.authservice.dto.response.AuthResponse;
import com.d1ff.authservice.entity.User;
import com.d1ff.authservice.exceptions.UserAlreadyExistsException;
import com.d1ff.authservice.mapper.request.RegisterRequestMapper;
import com.d1ff.authservice.repository.UserRepository;
import com.d1ff.authservice.service.auth.interfaces.AuthService;
import com.d1ff.authservice.service.jwt.interfaces.JwtService;
import com.d1ff.authservice.security.UserPrincipal;
import com.d1ff.authservice.service.jwt.interfaces.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RegisterRequestMapper registerRequestMapper;

    @Override
    @Transactional
    public AuthResponse register(String ip, String userAgent , RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration attempt for existing email: {}", request.email());
            throw new UserAlreadyExistsException("User with email " + request.email() + " already exists");
        }

        User user = registerRequestMapper.authRequestToUser(passwordEncoder, request);
        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());


        //emailConfirmationProducer.sendEmailConfirmation(user.getId(), user.getEmail());
        //userRegisteredProducer.sendUserRegistered(user.getId(), user.getEmail(), request.username());

        //TODO: email confirmation
        AccessTokenResponse accessToken = jwtService.createToken(user);
        refreshTokenService.createRefreshToken(user, response);

        return AuthResponse.builder()
                .accessToken(accessToken.accessToken())
                .expiresAt(accessToken.expiresAt())
                .build();
    }

    @Override
    public AuthResponse login(String ip, String userAgent, LoginRequest request,  HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = ((UserPrincipal) authentication.getPrincipal()).getUser();

        log.info("User logged in successfully: {}", user.getEmail());

        //byte[] payload = AnalyticPayloadFactory.createPayload(ip, userAgent, user.getId(), AnalyticEventType.LOGIN);
        //analyticSentProducer.saveAnalytic(payload);

        AccessTokenResponse accessToken = jwtService.createToken(user);
        refreshTokenService.createRefreshToken(user, response);

        return AuthResponse.builder()
                .accessToken(accessToken.accessToken())
                .expiresAt(accessToken.expiresAt())
                .build();
    }

    @Override
    public AuthResponse refreshToken(String ip, String userAgent, String refreshToken, HttpServletResponse response) {
        String userId = refreshTokenService.findAndRevokeToken(refreshToken);
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        log.info("Token refreshed for user: {}", user.getEmail());

        AccessTokenResponse accessToken = jwtService.createToken(user);
        refreshTokenService.createRefreshToken(user, response);

        return AuthResponse.builder()
                .accessToken(accessToken.accessToken())
                .expiresAt(accessToken.expiresAt())
                .build();
    }

    @Override
    public void logout(String ip, String userAgent, String refreshToken) {
        String userId = refreshTokenService.findAndRevokeToken(refreshToken);
        log.info("User logged out: {}", userId);
    }

    @Override
    public void logoutAll(String ip, String userAgent, String email) {
        if(email == null){
            throw new BadCredentialsException("Unauthorized");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        log.info("All sessions terminated for user: {}", email);
        refreshTokenService.revokeAllUserTokens(user);
    }
}