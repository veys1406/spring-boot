package com.example.spboot;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.util.Date;

@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          RedisTemplate<Object, Object> redisTemplate,
                          UserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder,
                          AppUserRepository userRepository) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request){

        Authentication authed = authenticationManager.authenticate(// kullanicinin girdiklerini springe teslim eder
                new UsernamePasswordAuthenticationToken(request.getUsername(),
                request.getPassword())
        );

        String accessToken = jwtService.generateToken(// token uretir kullanici veriyleriyle(access)
                authed.getName(),
                authed.getAuthorities().iterator().next().getAuthority()
        );

        String refreshToken = jwtService.generateRefreshToken(// token uretir kullanici veriyleriyle(refresh)
                authed.getName()
        );

        redisTemplate.opsForValue().set( refreshToken, authed.getName(), Duration.ofDays(7));

        return new LoginResponse(accessToken,refreshToken);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, @RequestBody(required = false) RefreshRequest body){
        String header = request.getHeader("Authorization");
        String token = header.substring(7); // Bearer i attik
        Date exp = jwtService.extractExp(token);
        long kalanSure = exp.getTime() - System.currentTimeMillis(); // expiration zamanindan suanki zamani cikarttik
        redisTemplate.opsForValue().set(token,"blacklisted", Duration.ofMillis(kalanSure));// Duration turunden olmak zorundaymisiz set methodu icin

        if(body!=null && body.getToken() != null){// cikis yapinca refresh tokeni redisten siliyoz direkt
            redisTemplate.delete(body.getToken());
        }
    }

    @PostMapping("/refresh")
    public String refresh(@RequestBody RefreshRequest request) throws AccessDeniedException {
        String token = request.getToken();
        if(     jwtService.isTokenValid(token) &&
                jwtService.extractType(token).equals("refresh") &&
                redisTemplate.hasKey(token)){

            String username = jwtService.extractUsername(token);
            String role = userDetailsService.loadUserByUsername(username).getAuthorities().iterator().next().getAuthority();
            // userDetailsService den kullanicinin rol bilgilerini cekiyor
            return jwtService.generateToken(username,role);// refresh tokenden access token uretildi kullaniciya sifre sorulmadan
        }else{
            throw new AccessDeniedException("Gecersiz RefreshToken");
        }
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest registerRequest) throws AccessDeniedException {
        if(!userRepository.findByUsername(registerRequest.getUsername()).isPresent()){// isPresent ici dolu mu bos mu diye bakar
            AppUser appUser = new AppUser();
            appUser.setUsername(registerRequest.getUsername());
            appUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));// sifre encode edilerek saklanmali
            appUser.setRole("USER");// kullanici kendi rolunu belirleyemez
            userRepository.save(appUser);
        }else{
            throw new AccessDeniedException("Bu kullanici zaten kayitli");
        }
    }

}
