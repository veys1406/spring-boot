package com.example.spboot.service;

import com.example.spboot.dto.TurnstileResponse;
import com.example.spboot.exception.CustomException;
import com.example.spboot.exception.ErrorCode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Map;

@Service
public class TurnstileService {
    private final RestClient restClient;
    private final String secret;

    public TurnstileService(@Value("${turnstile.secret}") String secret) {
        this.secret = secret;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.restClient = RestClient.builder()
                .baseUrl("https://challenges.cloudflare.com")
                .requestFactory(factory)
                .build();
    }

    public void verify(String token) {
        TurnstileResponse res;
        try {
            res = restClient.post()
                    .uri("/turnstile/v0/siteverify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("secret", secret, "response", token))
                    .retrieve()
                    .body(TurnstileResponse.class);
        } catch (RestClientException e) {
            throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE, "Captcha service unavailable", ErrorCode.CAPTCHA_UNAVAILABLE);
        }
        if (res == null) {
            throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE, "Captcha service unavailable", ErrorCode.CAPTCHA_UNAVAILABLE);
        }
        if(!res.success()){
            throw new CustomException(HttpStatus.BAD_REQUEST, "Captcha verification failed", ErrorCode.CAPTCHA_FAILED);
        } 
    }
}
