package com.example.spboot;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)throws ServletException, IOException {

        String header = request.getHeader("Authorization"); //Authorization headerini al

        if(header!=null && header.startsWith("Bearer ")){
            header = header.substring(7);// Bearer i almasin diye. suan sadece JWT token
            if(jwtService.isTokenValid(header)){

                UsernamePasswordAuthenticationToken authedToken = new UsernamePasswordAuthenticationToken(
                        jwtService.extractUsername(header),
                        null,
                        List.of(new SimpleGrantedAuthority(jwtService.extractRole(header)))// springin kabul ettigi turden rol
                );

                SecurityContextHolder.getContext().setAuthentication(authedToken);
                //securityContext de kullanicinin adi ve rolunun bilgisi var sifreyi tutmuyoruz cunku zaten token dogrulandi
            }
        }

        filterChain.doFilter(request,response);// bu filterin isi bitti istegi sonraki filtera devrediyor

    }
}
