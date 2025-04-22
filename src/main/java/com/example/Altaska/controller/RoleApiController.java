package com.example.Altaska.controller;

import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Roles;
import com.example.Altaska.repositories.RolesRepository;
import com.example.Altaska.repositories.ProjectsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects/{projectId}/roles")
public class RoleApiController {

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private ProjectsRepository projectsRepository;

    // GET: получить все роли для проекта (и общие, где projectId = null)
    @GetMapping
    public List<Roles> getProjectRoles(@PathVariable Long projectId) {
        return rolesRepository.findByIdProject_IdOrIdProjectIsNull(projectId);
    }

    // POST: создать кастомную роль в проекте
    @PostMapping
    @Transactional
    public Roles createCustomRole(
            @PathVariable Long projectId,
            @RequestBody RoleRequest request
    ) {
        Optional<Projects> projectOpt = projectsRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Проект не найден");
        }

        Roles newRole = new Roles();
        newRole.setName(request.name);
        newRole.setPermissions(request.permissions);
        newRole.setIsCustom(true);
        newRole.setIdProject(projectOpt.get());

        return rolesRepository.save(newRole);
    }

    // DTO класс для парсинга JSON
    public static class RoleRequest {
        public String name;
        public Map<String, Object> permissions;  // Убедитесь, что тип данных "boolean"
    }
}
