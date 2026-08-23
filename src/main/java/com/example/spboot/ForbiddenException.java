package com.example.spboot;

public class ForbiddenException extends RuntimeException {// 403 forbidden
    public ForbiddenException(String message) {
        super(message);
    }
}
