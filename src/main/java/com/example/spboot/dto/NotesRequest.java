package com.example.spboot.dto;

import jakarta.validation.constraints.NotBlank;

public class NotesRequest {
    @NotBlank
    private String icerik;

    public String getIcerik() {
        return icerik;
    }

    public void setIcerik(String icerik) {
        this.icerik = icerik;
    }
}
