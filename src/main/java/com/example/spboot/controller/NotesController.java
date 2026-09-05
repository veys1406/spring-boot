package com.example.spboot.controller;

import com.example.spboot.dto.NotesRequest;
import com.example.spboot.dto.NotesResponse;
import com.example.spboot.service.NotesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class NotesController {
    private final NotesService notesService;

    public NotesController(NotesService notesService) {
        this.notesService = notesService;
    }

    @GetMapping("/notes/{id}")
    public NotesResponse getNote(@PathVariable String id, @AuthenticationPrincipal String username){
        return notesService.getNoteById(id,username);
    }

    @GetMapping("/mynotes")
    public List<NotesResponse> getNotes(@AuthenticationPrincipal String username){
        return notesService.getMyNotes(username);
    }

    @PostMapping("/notes")// Content typei application/json
    public void saveNote(@RequestBody NotesRequest noteRequest,// JSON dan geliyor
                        @RequestParam String imza, // URLden geliyor
                        @AuthenticationPrincipal String username){
        notesService.createNote(noteRequest.getIcerik(),imza,username);
    }

    @PostMapping("/notes/upload")// Content typei multipart/form-data
    public void saveNoteWithImage(@RequestParam String icerik,
                                @RequestParam String imza,
                                @RequestParam MultipartFile image,
                                @AuthenticationPrincipal String username) throws IOException{
        
        notesService.createNoteWithImage(icerik, imza, image.getBytes(), username);

    }

    @GetMapping("/mynotes/search")// get methodlarda requestParam kullaniliyomus?
    public List<NotesResponse> searchNotes(@RequestParam String keyword, @AuthenticationPrincipal String username ){
        return notesService.searchMyNotes(username,keyword);
    }
}
