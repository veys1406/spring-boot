package com.example.spboot.service;

import com.example.spboot.config.RateLimitConfig;
import com.example.spboot.dto.AppUserResponse;
import com.example.spboot.dto.LoginResponse;
import com.example.spboot.dto.MailUsername;
import com.example.spboot.dto.MessageResponse;
import com.example.spboot.entity.AppUser;
import com.example.spboot.exception.CustomException;
import com.example.spboot.exception.ErrorCode;
import com.example.spboot.repository.AppUserRepository;
import com.example.spboot.security.RedisKeys;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.jsonwebtoken.Claims;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bucket;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ProxyManager<byte[]> proxyManager;
    
    @Value("${ratelimit.registerMail.capacity}")
    private int mailCapacity;
    @Value("${ratelimit.registerMail.fillRate}")
    private int mailFillRate;
    @Value("${ratelimit.registerMail.window}")
    private Duration mailWindow;

    public AuthService( AuthenticationManager authenticationManager,
                        JwtService jwtService,
                        RedisTemplate<Object, Object> redisTemplate,
                        UserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder,
                        AppUserRepository userRepository,
                        RabbitTemplate rabbitTemplate,
                        ProxyManager<byte[]> proxyManager) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.proxyManager = proxyManager;
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

        redisTemplate.opsForValue().set(RedisKeys.REFRESH_PREFIX+refreshToken, authed.getName(), Duration.ofDays(7));

        String key = RedisKeys.SESSIONS_PREFIX + authed.getName();
        redisTemplate.opsForSet().add(key, refreshToken);
        redisTemplate.expire(key, Duration.ofDays(7));

        return new LoginResponse(accessToken, refreshToken);
    }


    public String refresh(String token){
        if  ( jwtService.tokenStatus(token) == JwtService.TokenStatus.VALID ){

            Claims claim = jwtService.parseClaims(token);
            if(     jwtService.extractType(claim).equals("refresh") &&
                    redisTemplate.hasKey(RedisKeys.REFRESH_PREFIX+token)){

                String username = jwtService.extractUsername(claim);
                String role = userDetailsService.loadUserByUsername(username).getAuthorities().iterator().next().getAuthority();
                // userDetailsService den kullanicinin rol bilgilerini cekiyor

                String storedUsername = (String) redisTemplate.opsForValue().get(RedisKeys.REFRESH_PREFIX+token);
                if(!username.equals(storedUsername)){
                    throw new CustomException(HttpStatus.UNAUTHORIZED,"Invalid Refresh Token", ErrorCode.INVALID_REFRESH_TOKEN);
                }

                return jwtService.generateToken(username,role);// refresh tokenden access token uretildi kullaniciya sifre sorulmadan
            }
        }
        throw new CustomException(HttpStatus.UNAUTHORIZED,"Invalid Refresh Token", ErrorCode.INVALID_REFRESH_TOKEN);
    }


    public MessageResponse register(String username,String userMail, String password) {
        if(!userRepository.findByUsername(username).isPresent()){// isPresent ici dolu mu bos mu diye bakar

            String key = "ratelimit:registerMail:" +userMail.toLowerCase(Locale.ROOT);
            byte[] keyBytes = key.getBytes();
            BucketConfiguration config = RateLimitConfig.configOf(mailCapacity, mailFillRate, mailWindow);
            Bucket bucket = proxyManager.builder().build(keyBytes, () -> config);
            if(!bucket.tryConsume(1)){
                throw new CustomException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests", ErrorCode.RATE_LIMITED);
            }

            AppUser appUser = new AppUser();
            appUser.setUsername(username);
            appUser.setUserMail(userMail);
            appUser.setPassword(passwordEncoder.encode(password));// sifre encode edilerek saklanmali
            appUser.setRole("USER");// kullanici kendi rolunu belirleyemez
            userRepository.save(appUser);
            MailUsername mailUsername = new MailUsername(userMail, username);
            rabbitTemplate.convertAndSend("user.registered","kullanici kaydoldu", mailUsername, m -> {
                m.getMessageProperties().setMessageId(UUID.randomUUID().toString());
                return m;
            });

        }else{
            throw new CustomException(HttpStatus.CONFLICT,"This user is already exist!", ErrorCode.USER_EXISTS);
        }
        return new MessageResponse("Register Successfull!");
    }


    public MessageResponse logout(String accessToken, String refreshToken){

        if(accessToken != null && jwtService.tokenStatus(accessToken) == JwtService.TokenStatus.VALID){
            Claims claim = jwtService.parseClaims(accessToken);
            Date exp = jwtService.extractExp(claim);
            long kalanSure = exp.getTime() - System.currentTimeMillis(); // expiration zamanindan suanki zamani cikarttik
            redisTemplate.opsForValue().set(RedisKeys.BLACKLIST_PREFIX+accessToken, "blacklisted", Duration.ofMillis(kalanSure));// Duration turunden olmak zorundaymisiz set methodu icin
        }

        if(refreshToken != null){// cikis yapinca refresh tokeni redisten siliyoz direkt
            String username = (String) redisTemplate.opsForValue().getAndDelete(RedisKeys.REFRESH_PREFIX+refreshToken);
            if(username != null){
                redisTemplate.opsForSet().remove(RedisKeys.SESSIONS_PREFIX + username, refreshToken);
            }
        } 
        return new MessageResponse("Çıkış Başarılı");
    }


    public AppUserResponse me(Authentication authentication){
        return new AppUserResponse(authentication.getName(), authentication.getAuthorities().iterator().next().getAuthority());
    }

    public MessageResponse logoutAll(Authentication authentication){
        String username = authentication.getName();
        String sessionKey = RedisKeys.SESSIONS_PREFIX + username;
        Set<Object> tokens = redisTemplate.opsForSet().members(sessionKey);
        for (Object t : tokens){
            redisTemplate.delete(RedisKeys.REFRESH_PREFIX + t);
        }
        redisTemplate.delete(sessionKey);
        //access token icin logoutall yaptigi saati yaziyoruz redise
        String logoutKey = RedisKeys.LOGGEDOUT_PREFIX + username;
        redisTemplate.opsForValue().set(logoutKey, Instant.now().getEpochSecond(), Duration.ofMinutes(30));// suanki saniye
        
        return new MessageResponse("Tüm cihazlardan çıkış yapıldı");
    }
}
