package com.d1ff.exceptions;

public class NotAllowedFileException extends RuntimeException {
    public NotAllowedFileException(String message) {
        super(message);
    }
}
