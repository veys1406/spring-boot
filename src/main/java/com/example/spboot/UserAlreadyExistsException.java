package com.example.spboot;

public class UserAlreadyExistsException extends RuntimeException {// 409 conflict
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
