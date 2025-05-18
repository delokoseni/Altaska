package com.example.Altaska.controller;

import com.example.Altaska.dto.Change;
import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Tags;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.ProjectsRepository;
import com.example.Altaska.repositories.TagsRepository;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.services.ActivityLogService;
import com.example.Altaska.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
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

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Tags>> getTagsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(tagsRepository.findByIdProjectId(projectId));
    }

    @PostMapping("/create/{projectId}")
    public ResponseEntity<Tags> createTag(@PathVariable Long projectId, @RequestParam String name, Principal principal) {
        Projects project = projectsRepository.findById(projectId).orElse(null);
        if (project == null) return ResponseEntity.notFound().build();
        Tags tag = new Tags();
        tag.setName(name);
        tag.setIdProject(project);
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        permissionService.checkIfProjectArchived(tag.getIdProject());
        if (!permissionService.hasPermission(user.getId(), project.getId(), "create_tags")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав.");
        }
        Tags savedTag = tagsRepository.save(tag);

        // Логируем создание тега
        activityLogService.logActivity(
                user,
                project,
                "create",
                "tag",
                savedTag.getId(),
                null,
                "Создан тег \"" + savedTag.getName() + "\" в проекте \"" + project.getName() + "\""
        );

        return ResponseEntity.ok(savedTag);
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long tagId, Principal principal) {
        Tags tag = tagsRepository.findById(tagId)
                .orElse(null);
        if (tag == null) return ResponseEntity.notFound().build();
        Projects project = tag.getIdProject();
        if (project == null) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        permissionService.checkIfProjectArchived(project);
        if (!permissionService.hasPermission(user.getId(), project.getId(), "delete_tags")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав.");
        }
        activityLogService.logActivity(
                user,
                project,
                "delete",
                "tag",
                tag.getId(),
                null,
                "Удалён тег \"" + tag.getName() + "\" из проекта \"" + project.getName() + "\""
        );
        tagsRepository.deleteById(tagId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update/{tagId}")
    public ResponseEntity<Tags> updateTag(@PathVariable Long tagId, @RequestParam String name, Principal principal) {
        Tags tag = tagsRepository.findById(tagId).orElse(null);
        if (tag == null) return ResponseEntity.notFound().build();
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        permissionService.checkIfProjectArchived(tag.getIdProject());
        if (!permissionService.hasPermission(user.getId(), tag.getIdProject().getId(), "edit_tags")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав.");
        }
        String oldName = tag.getName();
        tag.setName(name);
        Tags updatedTag = tagsRepository.save(tag);
        if (!oldName.equals(name)) {
            activityLogService.logActivity(
                    user,
                    tag.getIdProject(),
                    "edit",
                    "tag",
                    updatedTag.getId(),
                    List.of(new Change("name", oldName, name)),
                    "Тег обновлён с \"" + oldName + "\" на \"" + name + "\" в проекте \"" + tag.getIdProject().getName() + "\""
            );
        }
        return ResponseEntity.ok(updatedTag);
    }
}
