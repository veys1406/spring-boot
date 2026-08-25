package com.example.spboot.exception;

public class InvalidTokenException extends RuntimeException {// 401 unauthorized
    public InvalidTokenException(String message) {
        super(message);
    }
}
