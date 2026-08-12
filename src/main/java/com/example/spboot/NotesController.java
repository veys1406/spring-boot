package com.example.spboot;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NotesController {
    private final NotesService notesService;

    public NotesController(NotesService notesService) {
        this.notesService = notesService;
    }

    @GetMapping("/notes/{id}")
    public Notes getNote(@PathVariable String id, @AuthenticationPrincipal String username){
        return notesService.getNoteById(id,username);
    }

    @GetMapping("/mynotes")
    public List<Notes> getGrades(@AuthenticationPrincipal String username){
        return notesService.getMyNotes(username);
    }

    @PostMapping("/notes")
    public void saveNote(@RequestBody NotesRequest noteRequest, @AuthenticationPrincipal String username){
        notesService.createNote(noteRequest.getIcerik(),username);
    }
}
