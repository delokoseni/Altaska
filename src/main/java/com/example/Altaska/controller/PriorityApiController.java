package com.example.Altaska.controller;

import com.example.Altaska.models.Priorities;
import com.example.Altaska.repositories.PrioritiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/priorities")
public class PriorityApiController {

    @Autowired
    private PrioritiesRepository prioritiesRepository;

    @GetMapping
    public List<Priorities> getAllPriorities() {
        return prioritiesRepository.findAll();
    }
}

