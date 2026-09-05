package com.example.spboot.service;

import com.example.spboot.entity.Notes;
import com.example.spboot.exception.CustomException;
import com.example.spboot.dto.NotesResponse;
import com.example.spboot.repository.NotesRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

        List<NotesResponse> result =  notesList.stream()
                .map(notes -> new NotesResponse(notes.getId(),notes.getIcerik(),notes.getImza(),notes.getImage()))
                .collect(Collectors.toList());

        return result;
    }

    public NotesResponse getNoteById(String id, String username) throws CustomException {
        Notes notes = repo.findById(id).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,"Not bulunamadi!"));
        if(!notes.getOwnerUsername().equals(username)){// IDOR
            throw new CustomException(HttpStatus.FORBIDDEN,"Bu Not sana ait degil!");
        }
        return new NotesResponse(notes.getId(),notes.getIcerik(),notes.getImza(),notes.getImage());
    }

    public Notes createNote(String icerik, String imza, String ownerUsername){
        Notes notes = new Notes();
        notes.setIcerik(icerik);
        notes.setImza(imza);
        notes.setOwnerUsername(ownerUsername);
        return repo.save(notes);// Mongo kendisi hallediyor ekliyor database e
    }

    public Notes createNoteWithImage(String icerik, String imza, byte[] image, String ownerUsername){
        Notes note = new Notes();
        note.setIcerik(icerik);
        note.setImza(imza);
        note.setImage(image);
        note.setOwnerUsername(ownerUsername);
        return repo.save(note);
    }

    public List<NotesResponse> searchMyNotes(String username, String keyword){
        List<Notes> notesList = repo.searchByKeyword(username,keyword);
        return notesList.stream()// STREAM API
                .map(notes -> new NotesResponse(notes.getId(),notes.getIcerik(),notes.getImza(),notes.getImage()))
                .collect(Collectors.toList());
    }

}
