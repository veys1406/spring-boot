package com.example.spboot;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
public class AnaController {
    private final AnaService service;

    public AnaController(AnaService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String returnString() {
        return "First message.";
    }

    @GetMapping("/merhaba/{name}")
    public String returnName(@PathVariable String name) {
        return service.returnName(name);
    }

    @GetMapping("/isimler")
    public ArrayList<String> returnList() {
        return service.returnList();
    }

    @PostMapping("/isimler")
    public void addList(@RequestBody String name) {
        service.addList(name);
    }

}
