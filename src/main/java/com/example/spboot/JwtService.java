package com.example.spboot;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtService {
    private final SecretKey secretKey = Keys.hmacShaKeyFor("supersecretkeysupersecretkeysupersecretkey".getBytes());

    public String generateToken(String username, String role){// tokeni yaratmak icin
         return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())// suanki zaman
                .expiration(new Date(System.currentTimeMillis() + 1000*60*30))// suanki zaman + yarim saat
                 .claim("role",role)
                .signWith(secretKey)// header ve payloade gore secret key ile imzaliyor
                .compact();// tokeni olusturup string doner
    }

    public boolean isTokenValid(String token){// girilen token ile imza ve sure eslesip eslesmedigine bakar
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);// hem sureyi hem de imzayi kontrol eder tutmazsa exception atar
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token){
        if(isTokenValid(token)){
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);//sure ve imza tutuyorsa jwt tokenini doner.

            return jws.getPayload().getSubject();// tokenin payload kismindaki username kismi
        }else{
            return null;
        }
    }

}
