package com.example.spboot;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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

}
