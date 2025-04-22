package com.example.Altaska.controller;

import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Roles;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.RolesRepository;
import com.example.Altaska.repositories.ProjectsRepository;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.services.PermissionService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects/{projectId}/roles")
public class RoleApiController {

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private ProjectsRepository projectsRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UsersRepository usersRepository;

    public record RoleDto(
            Long id,
            String name,
            boolean isCustom,
            JsonNode permissions,
            Long idProject
    ) {}

    public static class RoleRequest {
        public String name;
        public JsonNode permissions;
    }

    @GetMapping
    public List<RoleDto> getProjectRoles(@PathVariable Long projectId) {
        return rolesRepository.findByIdProject_IdOrIdProjectIsNull(projectId)
                .stream()
                .map(role -> new RoleDto(
                        role.getId(),
                        role.getName(),
                        role.getIsCustom(),
                        role.getPermissions(),
                        role.getIdProject() != null ? role.getIdProject().getId() : null
                ))
                .toList();
    }

    @PostMapping
    @Transactional
    public RoleDto createCustomRole(
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

        Roles savedRole = rolesRepository.save(newRole);

        return new RoleDto(
                savedRole.getId(),
                savedRole.getName(),
                savedRole.getIsCustom(),
                savedRole.getPermissions(),
                savedRole.getIdProject() != null ? savedRole.getIdProject().getId() : null
        );
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long projectId, @PathVariable Long roleId, Principal principal) {
        Optional<Roles> roleOpt = rolesRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Roles role = roleOpt.get();

        if (role.getIdProject() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Глобальные роли не могут быть удалены");
        }

        Projects project = projectsRepository.findById(role.getIdProject().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден"));

        permissionService.checkIfProjectArchived(project);

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        boolean hasPermission = permissionService.hasPermission(user.getId(), project.getId(), "EDIT_ROLE");
        if (!hasPermission) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав для удаления роли");
        }

        rolesRepository.delete(role);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{roleId}")
    @Transactional
    public RoleDto updateRole(
            @PathVariable Long projectId,
            @PathVariable Long roleId,
            @RequestBody RoleRequest request,
            Principal principal
    ) {
        Roles role = rolesRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Роль не найдена"));

        if (role.getIdProject() == null || !role.getIdProject().getId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Глобальные роли нельзя редактировать");
        }

        Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден"));

        permissionService.checkIfProjectArchived(project);

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!permissionService.hasPermission(user.getId(), projectId, "EDIT_ROLE")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав");
        }

        role.setName(request.name);
        role.setPermissions(request.permissions);

        Roles updatedRole = rolesRepository.save(role);

        return new RoleDto(
                updatedRole.getId(),
                updatedRole.getName(),
                updatedRole.getIsCustom(),
                updatedRole.getPermissions(),
                updatedRole.getIdProject() != null ? updatedRole.getIdProject().getId() : null
        );
    }

}
