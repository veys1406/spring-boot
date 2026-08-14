package com.example.spboot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableMethodSecurity // PreAuthorize in calismasi icin
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthenticationFilter jwtAuthFilter) throws Exception{
        http.csrf(csrf->csrf.disable())// csrf ayari nesnesini ver disable methodu calistir
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
