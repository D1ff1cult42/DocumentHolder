package com.d1ff.authservice.exceptions;

public class TokenException extends RuntimeException {
    public TokenException(String message) {
        super(message);
    }
}
