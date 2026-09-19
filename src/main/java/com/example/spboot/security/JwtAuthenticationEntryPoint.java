package com.example.spboot.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.spboot.exception.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class JwtAuthenticationEntryPoint implements  AuthenticationEntryPoint {
    
    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        Object authError = request.getAttribute("authError");
        ErrorCode code;
        if (authError != null) {
            code = (ErrorCode) authError;
        }else if (authException instanceof BadCredentialsException){
            code = ErrorCode.INVALID_CREDENTIALS;
        }else {
            code = ErrorCode.UNAUTHENTICATED;
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Kimlik dogrulanamadi");
        problem.setProperty("code", code);
        response.setStatus(401);
        response.setContentType("application/problem+json");
        
        objectMapper.writeValue(response.getWriter(), problem);
    }
}
