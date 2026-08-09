package com.example.spboot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnaController {
    @GetMapping("/")
    public String returnString() {
        return "First message.";
    }
    @GetMapping("/merhaba/{name}")
    public String returnName(@PathVariable String name ) {
        return "Selam " + name;
    }
}
