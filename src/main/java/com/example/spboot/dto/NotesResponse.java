package com.example.spboot.dto;

public class NotesResponse {
    private String icerik;

    public NotesResponse(String icerik) {
        this.icerik = icerik;
    }

    public String getIcerik() {
        return icerik;
    }
}
