package com.example.Altaska.controller;

import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Tags;
import com.example.Altaska.repositories.ProjectsRepository;
import com.example.Altaska.repositories.TagsRepository;
import com.example.Altaska.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @Autowired
    private PermissionService permissionService;

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
        permissionService.checkIfProjectArchived(tag.getIdProject());
        return ResponseEntity.ok(tagsRepository.save(tag));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long tagId) {
        Tags tag = tagsRepository.findById(tagId)
                .orElse(null);
        if (tag == null) return ResponseEntity.notFound().build();
        Projects project = tag.getIdProject();
        if (project == null) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        permissionService.checkIfProjectArchived(project);
        tagsRepository.deleteById(tagId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/update/{tagId}")
    public ResponseEntity<Tags> updateTag(@PathVariable Long tagId, @RequestParam String name) {
        Tags tag = tagsRepository.findById(tagId).orElse(null);
        if (tag == null) return ResponseEntity.notFound().build();
        tag.setName(name);
        permissionService.checkIfProjectArchived(tag.getIdProject());
        return ResponseEntity.ok(tagsRepository.save(tag));
    }
}
