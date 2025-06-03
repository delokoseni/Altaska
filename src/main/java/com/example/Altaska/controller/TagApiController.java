package com.example.Altaska.controller;

import com.example.Altaska.dto.Change;
import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Tags;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.ProjectsRepository;
import com.example.Altaska.repositories.TagsRepository;
import com.example.Altaska.repositories.TasksTagsRepository;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.services.ActivityLogService;
import com.example.Altaska.services.PermissionService;
import jakarta.transaction.Transactional;
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

    @Autowired
    private TasksTagsRepository tasksTagsRepository;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Tags>> getTagsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(tagsRepository.findByIdProjectId(projectId));
    }

    @PostMapping("/create/{projectId}")
    public ResponseEntity<?> createTag(@PathVariable Long projectId, @RequestParam String name, Principal principal) {
        Projects project = projectsRepository.findById(projectId).orElse(null);
        if (project == null) return ResponseEntity.notFound().build();
        if(name == null || name.length() > 20) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Длина названия тэга не может быть больше 20 символов!");
        }
        Tags tag = new Tags();
        tag.setName(name);
        tag.setIdProject(project);
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        if(permissionService.checkIfProjectArchived(tag.getIdProject()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        if (!permissionService.hasPermission(user.getId(), project.getId(), "create_tags")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        Tags savedTag = tagsRepository.save(tag);

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
    @Transactional
    public ResponseEntity<?> deleteTag(@PathVariable Long tagId, Principal principal) {
        Tags tag = tagsRepository.findById(tagId)
                .orElse(null);
        if (tag == null) return ResponseEntity.notFound().build();
        Projects project = tag.getIdProject();
        if (project == null) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        if(permissionService.checkIfProjectArchived(project))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        if (!permissionService.hasPermission(user.getId(), project.getId(), "delete_tags")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        tasksTagsRepository.deleteByIdTag(tag);
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
    public ResponseEntity<?> updateTag(@PathVariable Long tagId, @RequestParam String name, Principal principal) {
        Tags tag = tagsRepository.findById(tagId).orElse(null);
        if (tag == null) return ResponseEntity.notFound().build();
        if(name == null || name.length() > 20) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Длина названия тэга не может быть больше 20 символов!");
        }
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        if(permissionService.checkIfProjectArchived(tag.getIdProject()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        if (!permissionService.hasPermission(user.getId(), tag.getIdProject().getId(), "edit_tags")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
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
