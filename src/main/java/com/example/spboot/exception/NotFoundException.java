package com.example.spboot.exception;

public class NotFoundException extends RuntimeException {// 404 not found
    public NotFoundException(String message) {
        super(message);
    }
}
