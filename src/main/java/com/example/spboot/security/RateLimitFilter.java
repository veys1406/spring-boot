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

    @Value("${ratelimit.login.capacity}")
    private int capacity;
    @Value("${ratelimit.login.fillRate}")
    private int fillRate;
    @Value("${ratelimit.login.window}")
    private Duration window;

    public RateLimitFilter(ProxyManager<byte[]> proxyManager, ObjectMapper objectMapper) {
        this.proxyManager = proxyManager;
        this.objectMapper = objectMapper;
    }

    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if(!request.getRequestURI().equals("/login")){
            filterChain.doFilter(request, response);
        }else{
            String userIP = request.getRemoteAddr();
            String key = "ratelimit:login:" + userIP;
            byte[] keyBytes = key.getBytes();

            Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(fillRate,window));
            BucketConfiguration config = BucketConfiguration.builder()
                    .addLimit(limit)
                    .build();
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
    }
}
