package com.example.Altaska.controller;

import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Roles;
import com.example.Altaska.repositories.RolesRepository;
import com.example.Altaska.repositories.ProjectsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects/{projectId}/roles")
public class RoleApiController {

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private ProjectsRepository projectsRepository;

    @GetMapping
    public List<Roles> getProjectRoles(@PathVariable Long projectId) {
        return rolesRepository.findByIdProject_IdOrIdProjectIsNull(projectId);
    }

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

    public static class RoleRequest {
        public String name;
        public JsonNode permissions;
    }
}
