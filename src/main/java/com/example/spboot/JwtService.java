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
                .expiration(new Date(System.currentTimeMillis() + 1000*60*30))// suanki zaman + 30dk
                 .claim("role",role)
                 .claim("type","access")
                .signWith(secretKey)// header ve payloade gore secret key ile imzaliyor
                .compact();// tokeni olusturup string doner
    }

    public String generateRefreshToken(String username){// tokeni yaratmak icin
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())// suanki zaman
                .expiration(new Date(System.currentTimeMillis() + 1000*60*60*24*7))//7 gun
                .claim("type","refresh")
                .signWith(secretKey)
                .compact();
    }

    public boolean isTokenValid(String token){// girilen token ile imza ve sure eslesip eslesmedigine bakar
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims parseClaims(String token){// her extract methodunda ve isvalid de parselamak yerine hepsini bu methoda dayandiriyoruz
        return  Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();// hem sureyi hem de imzayi kontrol eder tutmazsa exception atar
    }

    public String extractUsername(Claims claims){
        return claims.getSubject();
    }

    public String extractRole(Claims claims){
        return claims.get("role",String.class);
    }

    public Date extractExp(Claims claims){
        return claims.getExpiration();
    }

    public String extractType(Claims claims){
        return claims.get("type", String.class);
    }

}
