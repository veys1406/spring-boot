package com.example.spboot;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotesService {
    private final NotesRepository repo;

    public NotesService(NotesRepository repo) {
        this.repo = repo;
    }

    public List<NotesResponse> getMyNotes(String username){
        List<Notes> notesList = repo.findByOwnerUsername(username);

        List<NotesResponse> result = new ArrayList<>();
        /*
        for(Notes n : notesList){
            result.add(new NotesResponse(n.getIcerik()));
        }
        */
        //STREAM API ustteki for-each dongusunun aynisini yapiyor
        //onceden entitityi direkt donuyorduk simdi DTO olusturkduk onu donuyoz
        result = notesList.stream()
                .map(notes -> new NotesResponse(notes.getIcerik()))
                .collect(Collectors.toList());

        return result;
    }

    public NotesResponse getNoteById(String id, String username) throws AccessDeniedException {
        Notes note = repo.findById(id).orElseThrow();
        if(!note.getOwnerUsername().equals(username)){// IDOR
            throw new AccessDeniedException("Bu Note sana ait degil!");
        }
        return new NotesResponse(note.getIcerik());
    }

    public Notes createNote(String icerik, String imza, String ownerUsername){
        Notes note = new Notes();
        note.setIcerik(icerik);
        note.setOwnerUsername(ownerUsername);
        return repo.save(note);// JPA kendisi hallediyor ekliyor database e
    }

}
