package com.example.spboot.dto;

public class NotesResponse {
    private String id;
    private String icerik;
    private String imza;
    private byte[] image;

    public NotesResponse(String id, String icerik, String imza, byte[] image) {
        this.id = id;
        this.icerik = icerik;
        this.imza = imza;
        this.image = image;
    }
    public String getId() {
        return id;
    }
    public String getIcerik() {
        return icerik;
    }
    public String getImza() {
        return imza;
    }
    public byte[] getImage() {
        return image;
    }


    
}
