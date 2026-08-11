package com.example.spboot;

import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AnaService {
    private final AnaRepository repo;

    public AnaService(AnaRepository repo) {
        this.repo = repo;
    }

    public String returnName(String name ) {
        return "Selam " + name;
    }

    public ArrayList<String> returnList() {
        return repo.returnList();
    }

    public void addList(String name) {
        repo.addList(name);
    }
}
