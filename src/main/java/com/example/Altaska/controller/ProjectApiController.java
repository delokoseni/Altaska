package com.example.Altaska.controller;

import com.example.Altaska.models.ProjectMembers;
import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Roles;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.ProjectMembersRepository;
import com.example.Altaska.repositories.ProjectsRepository;
import com.example.Altaska.repositories.RolesRepository;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectApiController {

    @Autowired
    private ProjectsRepository projectsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ProjectMembersRepository projectMembersRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Projects> getProjectById(@PathVariable Long id) {
        return projectsRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id,
                                           @RequestParam(required = false) String name,
                                           @RequestParam(required = false) String description,
                                           Principal principal) {
        Projects project = projectsRepository.findById(id).orElseThrow(() -> new RuntimeException("Проект не найден"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!permissionService.hasPermission(user.getId(), project.getId(), "edit")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Нет доступа");
        }

        permissionService.checkIfProjectArchived(project);

        if (name != null) project.setName(name);
        if (description != null) project.setDescription(description);
        projectsRepository.save(project);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<?> getProjectMembers(@PathVariable Long id) {
        List<ProjectMembers> members = projectMembersRepository.findByIdProjectId(id);

        List<Map<String, Object>> result = members.stream().map(member -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", member.getIdUser().getId());
            map.put("email", member.getIdUser().getEmail());

            Roles role = member.getIdRole();
            if (role != null) {
                map.put("roleId", role.getId());
                map.put("roleName", role.getName());
            } else {
                map.put("roleId", null);
                map.put("roleName", "Без роли");
            }

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }


    @PostMapping("/archive/{id}")
    public ResponseEntity<?> toggleArchiveStatus(@PathVariable Long id,
                                                 @RequestBody Map<String, Boolean> payload,
                                                 Principal principal) {
        Projects project = projectsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!permissionService.hasPermission(user.getId(), project.getId(), "edit")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Нет доступа");
        }

        boolean newStatus = payload.getOrDefault("archived", false);
        project.setIsArchived(newStatus);
        projectsRepository.save(project);
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{projectId}/members/{userId}/role")
    public ResponseEntity<?> changeMemberRole(@PathVariable Long projectId,
                                              @PathVariable Long userId,
                                              @RequestBody Map<String, Long> body,
                                              Principal principal) {
        Long newRoleId = body.get("roleId");

        Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));

        Users currentUser = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!permissionService.hasPermission(currentUser.getId(), project.getId(), "EDIT_ROLE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав");
        }

        permissionService.checkIfProjectArchived(project);

        ProjectMembers member = projectMembersRepository.findByIdProjectIdAndIdUserId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("Участник не найден"));

        Roles newRole = rolesRepository.findById(newRoleId)
                .orElseThrow(() -> new RuntimeException("Роль не найдена"));

        member.setIdRole(newRole);
        projectMembersRepository.save(member);

        return ResponseEntity.ok("Роль обновлена");
    }

}

