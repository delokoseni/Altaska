package com.example.Altaska.controller;

import com.example.Altaska.models.Statuses;
import com.example.Altaska.repositories.StatusesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statuses")
@RequiredArgsConstructor
public class StatusApiController {

    private final StatusesRepository statusesRepository;

    @GetMapping
    public List<Statuses> getAllStatuses() {
        return statusesRepository.findAll();
    }
}
