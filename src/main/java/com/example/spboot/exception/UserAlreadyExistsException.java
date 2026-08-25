package com.example.spboot.exception;

public class UserAlreadyExistsException extends RuntimeException {// 409 conflict
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
