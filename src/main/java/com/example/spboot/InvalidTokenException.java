package com.example.spboot;

public class InvalidTokenException extends RuntimeException {// 401 unauthorized
    public InvalidTokenException(String message) {
        super(message);
    }
}
