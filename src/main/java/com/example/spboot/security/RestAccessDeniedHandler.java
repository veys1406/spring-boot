package com.example.spboot.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import com.example.spboot.exception.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class RestAccessDeniedHandler implements  AccessDeniedHandler {
    
    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                        HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        
        ErrorCode code = ErrorCode.ACCESS_DENIED;
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Bu istek icin yetkin yok");

        if (accessDeniedException instanceof CsrfException) {
            code = ErrorCode.CSRF_INVALID;
            problem.setDetail("CSRF token gecersiz");
        }

        problem.setProperty("code", code);
        response.setStatus(403);
        response.setContentType("application/problem+json");
        
        objectMapper.writeValue(response.getWriter(), problem);
    }
}
