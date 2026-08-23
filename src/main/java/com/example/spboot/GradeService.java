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

    public Grade getGradeById(Long id, String username) {
        Grade grade = repo.findById(id).orElseThrow();// Optional<Grade> donerse exception firlatsin
        if(!grade.getOwnerUsername().equals(username)){// IDOR
            throw new ForbiddenException("This grade isn't yours");// CUSTOM EXCEPTION
        }
        return grade;
    }

    public void createGrade(String icerik, String ownerUsername){
        Grade grade = new Grade();
        grade.setIcerik(icerik);
        grade.setOwnerUsername(ownerUsername);
        repo.save(grade);
    }

}
