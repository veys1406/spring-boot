package com.example.spboot.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spboot.dto.MessageResponse;
import com.example.spboot.service.DlqService;

@RestController
public class DlqController {

    private final DlqService dlqService;

    public DlqController(DlqService dlqService) {
        this.dlqService = dlqService;
    }

    @PostMapping("/replay")
    public MessageResponse replay(@RequestParam(defaultValue = "10") int limit){
        return dlqService.replay(limit);
    }
}
