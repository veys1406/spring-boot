package com.example.spboot;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
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
    public void login(@RequestBody @Validated LoginRequest request, HttpServletResponse response){

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
        String token = null;

        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("accessToken")) {
                    token = c.getValue();// tokeni cookieden aliyoruz headerdan degil
                }
            }
        }
        //sadece tokeni nerden aldigimiz degisti gerisi ayni
        if(token != null){
            Claims claim = jwtService.parseClaims(token);
            Date exp = jwtService.extractExp(claim);
            long kalanSure = exp.getTime() - System.currentTimeMillis(); // expiration zamanindan suanki zamani cikarttik
            redisTemplate.opsForValue().set(token, "blacklisted", Duration.ofMillis(kalanSure));// Duration turunden olmak zorundaymisiz set methodu icin
        }

        if(body!=null && body.getToken() != null){// cikis yapinca refresh tokeni redisten siliyoz direkt
            redisTemplate.delete(body.getToken());
        }
    }

    @PostMapping("/refresh")
    public String refresh(@RequestBody RefreshRequest request){
        String token = request.getToken();
        if  ( jwtService.isTokenValid(token) ){

            Claims claim = jwtService.parseClaims(token);
            if(     jwtService.extractType(claim).equals("refresh") &&
                    redisTemplate.hasKey(token)){

                String username = jwtService.extractUsername(claim);
                String role = userDetailsService.loadUserByUsername(username).getAuthorities().iterator().next().getAuthority();
                // userDetailsService den kullanicinin rol bilgilerini cekiyor
                return jwtService.generateToken(username,role);// refresh tokenden access token uretildi kullaniciya sifre sorulmadan
            }
        }
        throw new InvalidTokenException("Invalid Refresh Token");
    }

    @PostMapping("/register")
    public void register(@RequestBody @Validated RegisterRequest registerRequest) {
        if(!userRepository.findByUsername(registerRequest.getUsername()).isPresent()){// isPresent ici dolu mu bos mu diye bakar
            AppUser appUser = new AppUser();
            appUser.setUsername(registerRequest.getUsername());
            appUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));// sifre encode edilerek saklanmali
            appUser.setRole("USER");// kullanici kendi rolunu belirleyemez
            userRepository.save(appUser);
        }else{
            throw new UserAlreadyExistsException("This user is already exist");
        }
    }

}
