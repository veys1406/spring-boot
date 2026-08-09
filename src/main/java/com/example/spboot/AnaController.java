package com.example.spboot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnaController {
    @GetMapping("/")
    public String returnString() {
        return "First message.";
    }
}
