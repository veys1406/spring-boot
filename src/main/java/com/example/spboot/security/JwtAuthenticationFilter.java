package com.example.spboot.security;

import com.example.spboot.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RedisTemplate<Object, Object> redisTemplate;

    public JwtAuthenticationFilter(JwtService jwtService, RedisTemplate<Object, Object> redisTemplate) {
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)throws ServletException, IOException {

        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie[] cookies = request.getCookies();
        String token = null;

        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("accessToken")) {
                    token = c.getValue();
                }
            }
        }

        if(jwtService.isTokenValid(token)){
            Boolean isBlacklisted = redisTemplate.hasKey(token);// null donebilir o yuzden Boolean
            if(isBlacklisted == null || !isBlacklisted){
                Claims claims = jwtService.parseClaims(token);
                UsernamePasswordAuthenticationToken authedToken = new UsernamePasswordAuthenticationToken(
                        jwtService.extractUsername(claims),
                        null,
                        List.of(new SimpleGrantedAuthority(jwtService.extractRole(claims)))// springin kabul ettigi turden rol
                );
                SecurityContextHolder.getContext().setAuthentication(authedToken);
                //securityContext de kullanicinin adi ve rolunun bilgisi var sifreyi tutmuyoruz cunku zaten token dogrulandi
            }
        }


        filterChain.doFilter(request,response);// bu filterin isi bitti istegi sonraki filtera devrediyor

    }
}
