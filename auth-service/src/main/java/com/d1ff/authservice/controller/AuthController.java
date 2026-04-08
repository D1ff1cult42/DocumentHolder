package com.d1ff.authservice.controller;

import com.d1ff.authservice.dto.request.LoginRequest;
import com.d1ff.authservice.dto.request.RegisterRequest;
import com.d1ff.authservice.dto.response.AuthResponse;
import com.d1ff.authservice.service.auth.interfaces.AuthService;
import com.d1ff.authservice.utils.RefreshExtractor;
import com.d1ff.response.ErrorResponse;
import com.d1ff.utils.IpAndUserAgentResolver;
import com.d1ff.utils.dto.AnalyticDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "AuthController", description = "Controller for user authentication operations")
public class AuthController {
    private final AuthService authService;
    @Operation(summary = "Register a new user",
            description = "Register a new user with the provided email and password.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User registered successfully",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "User already exists",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest registerRequest,
                                                 HttpServletResponse response,
                                                 HttpServletRequest request){
        AnalyticDto dto = IpAndUserAgentResolver.resolve(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(dto.ip(), dto.userAgent(), registerRequest, response));
    }

    @Operation(summary = "User login",
            description = "Authenticate a user with email and password.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User logged in successfully",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest,
                                              HttpServletResponse response,
                                              HttpServletRequest request){
        AnalyticDto dto = IpAndUserAgentResolver.resolve(request);
        return ResponseEntity
                .ok(authService.login(dto.ip(), dto.userAgent(), loginRequest, response));
    }

    @Operation(summary = "Refresh authentication token",
            description = "Refresh the authentication token using a valid refresh token.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Token refreshed successfully",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid or expired refresh token",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
                                                     HttpServletResponse response,
                                                     HttpServletRequest request){
        AnalyticDto dto = IpAndUserAgentResolver.resolve(request);
        String refreshToken = RefreshExtractor.extract(request);
        return ResponseEntity
                .ok(authService.refreshToken(dto.ip(), dto.userAgent(), refreshToken, response));
    }

    @Operation(summary = "Logout user",
            description = "Logout the user by invalidating the provided refresh token.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "User logged out successfully"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid or expired refresh token",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid Credentials",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request){
        String refreshToken = RefreshExtractor.extract(request);
        AnalyticDto dto = IpAndUserAgentResolver.resolve(request);

        authService.logout(dto.ip(), dto.userAgent(), refreshToken);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Logout all sessions for a user",
            description = "Logout the user from all sessions by invalidating all refresh tokens associated with the user's email.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "All user sessions logged out successfully"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid Credentials",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(HttpServletRequest request){
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        AnalyticDto dto = IpAndUserAgentResolver.resolve(request);

        authService.logoutAll(dto.ip(), dto.userAgent(), email);

        return ResponseEntity.noContent().build();
    }
}
