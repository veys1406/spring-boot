package com.example.spboot.dto;

public class MailUsername {// queue ya Mail ve username donmek icin
    private String userMail;
    private String username;
    
    public MailUsername(String userMail, String username) {
        this.userMail = userMail;
        this.username = username;
    }
    public String getUserMail() {
        return userMail;
    }
    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    
}
