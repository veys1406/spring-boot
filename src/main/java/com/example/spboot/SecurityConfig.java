package com.example.spboot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableMethodSecurity // PreAuthorize in calismasi icin
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Value("${cors.origin}")
    private String corsOrigin;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthenticationFilter jwtAuthFilter) throws Exception{
        http    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf->csrf.disable())// csrf ayari nesnesini ver disable methodu calistir
                .authorizeHttpRequests(auth->auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll())//REQUEST MATCHERS
                .authorizeHttpRequests(auth->auth.requestMatchers("/login","/refresh","/register").permitAll()
                                                                           .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout->logout.disable());// Springin kendi logout mekanizmasini kapat
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();// spring kendi urettigi AuthenticationManager'i ver
    }

    /*asagidaki 2 method httpBasic calistigindaki filterin kullanacagi methodlar.
    kullaniciadi ve sifreyi ayirmayi arkaplanda BasicAuthenticationFilter yapiyor sonra
    AuthenticationManager o da AuthenticationProvider a veriyor kullanici bilgileri geliyo
    sifreler eslesiyo mu (matches()) bakiliyo tum bunlar SPRING SECURITYNIN IC MEKANIZMASI
    ben sadece bu mekanizmanin kullanacagi beanleri spring'e sagliyorum*/
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(); // spring'in kendi hash implementasyonu
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {//PROPERTIES ICINDE BURDA VALUE OLARK KULLAN
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(corsOrigin));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",configuration);
        return source;
    }

    /*@Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder){
        UserDetails veysel = User.builder()
                .username("veysel")
                .password(encoder.encode("veysel123"))
                .roles("USER")
                .build();
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(veysel, admin);
    }DATABASE'E GECTIM*/
}
