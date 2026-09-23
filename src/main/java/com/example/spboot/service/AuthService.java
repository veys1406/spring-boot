package com.example.spboot.service;

import com.example.spboot.dto.AppUserResponse;
import com.example.spboot.dto.LoginResponse;
import com.example.spboot.dto.MailUsername;
import com.example.spboot.dto.MessageResponse;
import com.example.spboot.entity.AppUser;
import com.example.spboot.exception.CustomException;
import com.example.spboot.exception.ErrorCode;
import com.example.spboot.repository.AppUserRepository;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.GetResponse;

import io.jsonwebtoken.Claims;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthService( AuthenticationManager authenticationManager,
                        JwtService jwtService,
                        RedisTemplate<Object, Object> redisTemplate,
                        UserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder,
                        AppUserRepository userRepository,
                        RabbitTemplate rabbitTemplate) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
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
        if  ( jwtService.tokenStatus(token) == JwtService.TokenStatus.VALID ){

            Claims claim = jwtService.parseClaims(token);
            if(     jwtService.extractType(claim).equals("refresh") &&
                    redisTemplate.hasKey(token)){

                String username = jwtService.extractUsername(claim);
                String role = userDetailsService.loadUserByUsername(username).getAuthorities().iterator().next().getAuthority();
                // userDetailsService den kullanicinin rol bilgilerini cekiyor

                String storedUsername = (String) redisTemplate.opsForValue().get(token);
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

    public MessageResponse replay(int messageCount){
        int safeLimit = Math.min(messageCount, 100);

        return rabbitTemplate.execute(channel -> {
            int replayed = 0;

            for(int i=0; i<safeLimit; i++){
                GetResponse response = channel.basicGet("garbage-queue", false);

                if(response == null) break;

                Map<String, Object> headers = response.getProps().getHeaders();
                List<Map<String, Object>> xDeath = (List<Map<String, Object>>) headers.get("x-death");
                Map<String, Object> lastDeath = xDeath.get(0);

                String exchange = lastDeath.get("exchange").toString();
                String routingKey = ((List<?>) lastDeath.get("routing-keys")).get(0).toString();

                Map<String, Object> newHeaders = new HashMap<>(headers);
                Object oldCount = newHeaders.get("x-replay-count");
                int replayCount = (oldCount == null) ? 0 : ((Number) oldCount).intValue();

                newHeaders.put("x-replay-count", replayCount + 1);

                AMQP.BasicProperties newProps = response.getProps().builder()
                .headers(newHeaders)
                .build();
                
                channel.basicPublish(exchange, routingKey, newProps, response.getBody());
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                log.info("{}", response);
                replayed++;
            }

            return new MessageResponse(replayed + " mesaj replay edildi");
        });
    }

}
