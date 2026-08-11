package com.example.spboot;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GradeController {
    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping("/grades/{id}")
    public Grade getGrade(@PathVariable Long id, @AuthenticationPrincipal String username){
        return gradeService.getGradeById(id,username);
    }

    @GetMapping("/mygrades")
    public List<Grade> getGrades(@AuthenticationPrincipal String username){
        return gradeService.getMyGrades(username);
    }
}
