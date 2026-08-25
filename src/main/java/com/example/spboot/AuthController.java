package com.example.spboot;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Date;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")// login() refresh token'ı frontend'e hiç iletmiyor
    public void login(@RequestBody @Validated LoginRequest request, HttpServletResponse response){

        String accessToken = authService.login(request.getUsername(),request.getPassword());

        ResponseCookie cookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMinutes(30))
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, @RequestBody(required = false) RefreshRequest body){

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

        authService.logout(accessToken,refreshToken);
    }

    @PostMapping("/refresh")
    public String refresh(@RequestBody RefreshRequest request){
        return authService.refresh(request.getToken());
    }

    @PostMapping("/register")
    public void register(@RequestBody @Validated RegisterRequest registerRequest) {
        authService.register( registerRequest.getUsername(), registerRequest.getPassword() );
    }

}
