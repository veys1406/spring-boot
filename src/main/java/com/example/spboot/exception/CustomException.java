package com.example.spboot.exception;

import org.springframework.http.HttpStatusCode;

public class CustomException extends RuntimeException {
    private HttpStatusCode status;
    private ErrorCode code;

    public CustomException(HttpStatusCode status, String message, ErrorCode code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public ErrorCode getCode() {
        return code;
    }
}
