package com.example.spboot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank
    private String username;
    @Email @NotBlank
    private String userMail;
    @NotBlank @Size(min = 6, max = 12)
    private String password;
    @NotBlank @Size(max = 2048)
    private String captchaToken;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getUserMail() {
        return userMail;
    }
    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getCaptchaToken() {
        return captchaToken;
    }
    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }
    
}
