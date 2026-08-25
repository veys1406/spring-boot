package com.example.spboot.exception;

public class ForbiddenException extends RuntimeException {// 403 forbidden
    public ForbiddenException(String message) {
        super(message);
    }
}
