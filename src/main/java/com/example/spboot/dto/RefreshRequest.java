package com.example.spboot.dto;

public class RefreshRequest{
    private String refreshToken;

    public String getToken() {
        return refreshToken;
    }

    public void setToken(String username) {
        this.refreshToken = username;
    }
}
