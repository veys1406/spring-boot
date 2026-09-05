package com.example.spboot.service;

import com.example.spboot.dto.AppUserResponse;
import com.example.spboot.dto.LoginResponse;
import com.example.spboot.dto.MessageResponse;
import com.example.spboot.entity.AppUser;
import com.example.spboot.exception.CustomException;
import com.example.spboot.repository.AppUserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository userRepository;

    public AuthService( AuthenticationManager authenticationManager,
                        JwtService jwtService,
                        RedisTemplate<Object, Object> redisTemplate,
                        UserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder,
                        AppUserRepository userRepository ) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public LoginResponse login(String username, String password){
        Authentication authed = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username,password)
        );

        String accessToken = jwtService.generateToken(// token uretir kullanici veriyleriyle(access)
                authed.getName(),
                authed.getAuthorities().iterator().next().getAuthority()
        );

        String refreshToken = jwtService.generateRefreshToken(// token uretir kullanici veriyleriyle(refresh)
                authed.getName()
        );

        redisTemplate.opsForValue().set( refreshToken, authed.getName(), Duration.ofDays(7));
        return new LoginResponse(accessToken, refreshToken);
    }

    public String refresh(String token){
        if  ( jwtService.isTokenValid(token) ){

            Claims claim = jwtService.parseClaims(token);
            if(     jwtService.extractType(claim).equals("refresh") &&
                    redisTemplate.hasKey(token)){

                String username = jwtService.extractUsername(claim);
                String role = userDetailsService.loadUserByUsername(username).getAuthorities().iterator().next().getAuthority();
                // userDetailsService den kullanicinin rol bilgilerini cekiyor

                String storedUsername = (String) redisTemplate.opsForValue().get(token);
                if(!username.equals(storedUsername)){
                    throw new CustomException(HttpStatus.UNAUTHORIZED,"Invalid Refresh Token");
                }

                return jwtService.generateToken(username,role);// refresh tokenden access token uretildi kullaniciya sifre sorulmadan
            }
        }
        throw new CustomException(HttpStatus.UNAUTHORIZED,"Invalid Refresh Token");
    }

    public void register(String username,String password) {
        if(!userRepository.findByUsername(username).isPresent()){// isPresent ici dolu mu bos mu diye bakar
            AppUser appUser = new AppUser();
            appUser.setUsername(username);
            appUser.setPassword(passwordEncoder.encode(password));// sifre encode edilerek saklanmali
            appUser.setRole("USER");// kullanici kendi rolunu belirleyemez
            userRepository.save(appUser);
        }else{
            throw new CustomException(HttpStatus.CONFLICT,"This user is already exist!");
        }
    }

    public MessageResponse logout(String accessToken, String refreshToken){

        if(accessToken != null){
            Claims claim = jwtService.parseClaims(accessToken);
            Date exp = jwtService.extractExp(claim);
            long kalanSure = exp.getTime() - System.currentTimeMillis(); // expiration zamanindan suanki zamani cikarttik
            redisTemplate.opsForValue().set(accessToken, "blacklisted", Duration.ofMillis(kalanSure));// Duration turunden olmak zorundaymisiz set methodu icin
        }

        if(refreshToken != null){// cikis yapinca refresh tokeni redisten siliyoz direkt
            redisTemplate.delete(refreshToken);
        } 
        return new MessageResponse("Çıkış Başarılı");
    }

    public AppUserResponse me(Authentication authentication){
        return new AppUserResponse(authentication.getName(), authentication.getAuthorities().iterator().next().getAuthority());
    }

}
