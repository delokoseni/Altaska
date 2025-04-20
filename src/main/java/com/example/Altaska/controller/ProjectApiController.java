package com.example.Altaska.controller;

import com.example.Altaska.models.Projects;
import com.example.Altaska.repositories.ProjectsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectApiController {

    @Autowired
    private ProjectsRepository projectsRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Projects> getProjectById(@PathVariable Long id) {
        return projectsRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id,
                                           @RequestParam(required = false) String name,
                                           @RequestParam(required = false) String description) {
        return projectsRepository.findById(id).map(project -> {
            if (name != null) project.setName(name);
            if (description != null) project.setDescription(description);
            projectsRepository.save(project);
            return ResponseEntity.ok(project);
        }).orElse(ResponseEntity.notFound().build());
    }

}

