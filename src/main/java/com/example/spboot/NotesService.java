package com.example.spboot;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotesService {
    private final NotesRepository repo;

    public NotesService(NotesRepository repo) {
        this.repo = repo;
    }

    public List<Notes> getMyNotes(String username){
        return repo.findByOwnerUsername(username);
    }

    public Notes getNoteById(String id, String username) throws AccessDeniedException {
        Notes note = repo.findById(id).orElseThrow();// Optional<Grade> donerse exception firlatsin
        if(!note.getOwnerUsername().equals(username)){// IDOR
            throw new AccessDeniedException("Bu Note sana ait degil!");
        }
        return note;
    }

    public Notes createNote(String icerik, String ownerUsername){
        Notes note = new Notes();
        note.setIcerik(icerik);
        note.setOwnerUsername(ownerUsername);
        return repo.save(note);// JPA kendisi hallediyor ekliyor database e
    }

}
