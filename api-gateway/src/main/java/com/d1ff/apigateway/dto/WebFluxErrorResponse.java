package com.d1ff.apigateway.dto;

import org.springframework.http.HttpStatus;

import java.sql.Timestamp;

public record WebFluxErrorResponse(
        String message,
        String error,
        int status,
        Timestamp timestamp,
        String path
) {
    public static WebFluxErrorResponse of(HttpStatus status, String message, String path) {
        return new WebFluxErrorResponse(
                message,
                status.getReasonPhrase(),
                status.value(),
                new Timestamp(System.currentTimeMillis()),
                path
        );
    }
}
