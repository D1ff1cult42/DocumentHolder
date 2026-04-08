package com.d1ff.authservice.exceptions;

import com.d1ff.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.Timestamp;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionAdvice {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex,
            HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .error("User Already Exists")
                .status(HttpStatus.CONFLICT.value())
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ErrorResponse> handleTokenException(
            TokenException ex,
            HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .error("Invalid or expired token")
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex,
            HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .error("Invalid Credentials")
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(message)
                .error("Validation Error")
                .status(400)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindErrors(
            BindException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(message)
                .error("Validation Error")
                .status(400)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unexpected error occurred: ", ex);
        ErrorResponse error = ErrorResponse.builder()
                .message("An unexpected error occurred")
                .error(ex.getClass().getSimpleName())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
