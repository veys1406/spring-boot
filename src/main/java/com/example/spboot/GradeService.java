package com.example.spboot;

import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;// Springin kendi exceptionu gorunce 403 verir
import java.util.List;

@Service
public class GradeService {
    private final GradeRepository repo;

    public GradeService(GradeRepository repo) {
        this.repo = repo;
    }

    public List<Grade> getMyGrades(String username){
        return repo.findByOwnerUsername(username);
    }

    public Grade getGradeById(Long id, String username) throws AccessDeniedException {
        Grade grade = repo.findById(id).orElseThrow();// Optional<Grade> donerse exception firlatsin
        if(!grade.getOwnerUsername().equals(username)){// IDOR
            throw new AccessDeniedException("Bu Grade sana ait degil!");
        }
        return grade;
    }

}
