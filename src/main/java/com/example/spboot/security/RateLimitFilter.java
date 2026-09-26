package com.example.spboot.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.spboot.exception.ErrorCode;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final ProxyManager<byte[]> proxyManager;
    private final ObjectMapper objectMapper;

    @Value("${ratelimit.loginIp.capacity}")
    private int loginCapacity;
    @Value("${ratelimit.loginIp.fillRate}")
    private int loginFillRate;
    @Value("${ratelimit.loginIp.window}")
    private Duration loginWindow;

    @Value("${ratelimit.registerIp.capacity}")
    private int registerCapacity;
    @Value("${ratelimit.registerIp.fillRate}")
    private int registerFillRate;
    @Value("${ratelimit.registerIp.window}")
    private Duration registerWindow;

    public RateLimitFilter(ProxyManager<byte[]> proxyManager, ObjectMapper objectMapper) {
        this.proxyManager = proxyManager;
        this.objectMapper = objectMapper;
    }

    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String userIP = request.getRemoteAddr();
        String key;
        BucketConfiguration config;
        if(requestURI.equals("/login")){
            key = "ratelimit:loginIp:" + userIP;
            config= configOf(loginCapacity, loginFillRate, loginWindow);

        }else if(requestURI.equals("/register")){
            key = "ratelimit:registerIp:" + userIP;
            config= configOf(registerCapacity, registerFillRate, registerWindow);

        }else{
            filterChain.doFilter(request, response);
            return;
        }

        //login ve registerda ortak 
        byte[] keyBytes = key.getBytes();
        Bucket bucket = proxyManager.builder().build(keyBytes, () -> config);

        boolean izinVar = bucket.tryConsume(1);
        if(!izinVar){
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, "Cok fazla istek attiniz");
            problem.setProperty("code", ErrorCode.RATE_LIMITED.name());
            response.setStatus(429);
            response.setContentType("application/problem+json");
            objectMapper.writeValue(response.getWriter(), problem);
        }else{
            filterChain.doFilter(request, response);
        }
    }

    private BucketConfiguration configOf(int capacity, int fillRate, Duration window) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(fillRate, window));
        return BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }
}
