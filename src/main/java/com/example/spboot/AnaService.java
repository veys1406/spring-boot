package com.example.spboot;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class AnaService {

    public String returnName(String name ) {
        return "Selam " + name;
    }
}
