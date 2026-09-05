package com.example.spboot.controller;

import com.example.spboot.service.AuthService;
import com.example.spboot.dto.AppUserResponse;
import com.example.spboot.dto.LoginRequest;
import com.example.spboot.dto.LoginResponse;
import com.example.spboot.dto.MessageResponse;
import com.example.spboot.dto.RefreshRequest;
import com.example.spboot.dto.RegisterRequest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")// login() refresh token'ı frontend'e hiç iletmiyor???
    public MessageResponse login(@RequestBody @Validated LoginRequest request, HttpServletResponse response){

        LoginResponse loginResponse = authService.login(request.getUsername(),request.getPassword());

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", loginResponse.getAccessToken())
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMinutes(30))
                .sameSite("Strict")// cookie attribute degistirdik. CSRF TOKEN MANTIK ANLASILDI KODA DOKULMEDI
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)
                .path("/refresh")// sadece refresh endpointine eklenir bu coookie otomatik
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return new MessageResponse("Giriş Başarılı");
    }

    @PostMapping("/logout")//
    public MessageResponse logout(HttpServletRequest request, @RequestBody(required = false) RefreshRequest body){

        Cookie[] cookies = request.getCookies();
        String accessToken = null;

        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("accessToken")) {
                    accessToken = c.getValue();// tokeni cookieden aliyoruz headerdan degil
                }
            }
        }

        String refreshToken = (body != null) ? body.getToken() : null;

        return authService.logout(accessToken,refreshToken);
    }

    @PostMapping("/refresh")
    public MessageResponse refresh(HttpServletRequest request, HttpServletResponse response){

        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie c : cookies) {// OPTIMIZASYON SORUNU?
                if (c.getName().equals("refreshToken")) {// tokeni cookieden aliyoruz headerdan degil
                    refreshToken = c.getValue();
                }
            }
        }
        String accessToken_new = authService.refresh(refreshToken);
        ResponseCookie cookie = ResponseCookie.from("accessToken", accessToken_new)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMinutes(30))
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return new MessageResponse("AccessToken guncellendi.(Refresh)");
    }

    @PostMapping("/register")
    public void register(@RequestBody @Validated RegisterRequest registerRequest) {
        authService.register( registerRequest.getUsername(), registerRequest.getPassword() );
    }

    @GetMapping("/me")
    public AppUserResponse me(Authentication authentication){
        return authService.me(authentication);
    }

}
