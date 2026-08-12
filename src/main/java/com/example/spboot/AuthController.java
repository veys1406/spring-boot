package com.example.spboot;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Date;

@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RedisTemplate<Object, Object> redisTemplate;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, RedisTemplate<Object, Object> redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request){

        Authentication authed = authenticationManager.authenticate(// kullanicinin girdiklerini springe teslim eder
                new UsernamePasswordAuthenticationToken(request.getUsername(),
                request.getPassword())
        );

        return jwtService.generateToken(// token uretir kullanici veriyleriyle
                authed.getName(),
                authed.getAuthorities().iterator().next().getAuthority()
        );
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        String token = header.substring(7); // Bearer i attik
        Date exp = jwtService.extractExp(token);
        long kalanSure = exp.getTime() - System.currentTimeMillis(); // expiration zamanindan suanki zamani cikarttik
        redisTemplate.opsForValue().set(token,"blacklisted", Duration.ofMillis(kalanSure));// Duration turunden olmak zorundaymisiz set methodu icin
    }

}
