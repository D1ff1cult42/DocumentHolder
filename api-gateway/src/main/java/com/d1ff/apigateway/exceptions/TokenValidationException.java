package com.d1ff.apigateway.exceptions;

public class TokenValidationException extends RuntimeException{
    public TokenValidationException(String message){
        super(message);
    }
}

