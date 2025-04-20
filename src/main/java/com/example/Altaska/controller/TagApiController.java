package com.example.Altaska.controller;

import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Tags;
import com.example.Altaska.repositories.ProjectsRepository;
import com.example.Altaska.repositories.TagsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagApiController {

    @Autowired
    private TagsRepository tagsRepository;

    @Autowired
    private ProjectsRepository projectsRepository;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Tags>> getTagsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(tagsRepository.findByIdProjectId(projectId));
    }

    @PostMapping("/create/{projectId}")
    public ResponseEntity<Tags> createTag(@PathVariable Long projectId, @RequestParam String name) {
        Projects project = projectsRepository.findById(projectId).orElse(null);
        if (project == null) return ResponseEntity.notFound().build();

        Tags tag = new Tags();
        tag.setName(name);
        tag.setIdProject(project);

        return ResponseEntity.ok(tagsRepository.save(tag));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long tagId) {
        if (!tagsRepository.existsById(tagId)) return ResponseEntity.notFound().build();
        tagsRepository.deleteById(tagId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update/{tagId}")
    public ResponseEntity<Tags> updateTag(@PathVariable Long tagId, @RequestParam String name) {
        Tags tag = tagsRepository.findById(tagId).orElse(null);
        if (tag == null) return ResponseEntity.notFound().build();
        tag.setName(name);
        return ResponseEntity.ok(tagsRepository.save(tag));
    }
}
