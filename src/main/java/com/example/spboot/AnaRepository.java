package com.example.spboot;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public class AnaRepository {
    private ArrayList<String> list = new ArrayList<String>(); // kapsulleme

    public AnaRepository() {
        list.add("Veysel");
        list.add("Selim");
    }

    public ArrayList<String> returnList() {
        return list;
    }

    public void addList(String name) {
        list.add(name);
    }
}
