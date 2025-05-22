package com.example.Altaska.controller;

import com.example.Altaska.dto.Change;
import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Roles;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.RolesRepository;
import com.example.Altaska.repositories.ProjectsRepository;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.services.ActivityLogService;
import com.example.Altaska.services.PermissionService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    @Autowired
    private ActivityLogService activityLogService;

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
    public ResponseEntity<?> createCustomRole(@PathVariable Long projectId, @RequestBody RoleRequest request, Principal principal) {
        Optional<Projects> projectOpt = projectsRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Проект не найден");
        }

        permissionService.checkIfProjectArchived(projectOpt.get());

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (request.name == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Имя роли не может быть пустым.");
        }
        if (request.name.length() > 100) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Имя роли не может быть длиннее 100 символов.");
        }

        boolean hasPermission = permissionService.hasPermission(user.getId(), projectOpt.get().getId(), "delete_roles");
        if (!hasPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }

        Roles newRole = new Roles();
        newRole.setName(request.name);
        newRole.setPermissions(request.permissions);
        newRole.setIsCustom(true);
        newRole.setIdProject(projectOpt.get());

        Roles savedRole = rolesRepository.save(newRole);
        activityLogService.logActivity(
                user,
                projectOpt.get(),
                "create",
                "role",
                savedRole.getId(),
                List.of(
                        new Change("permissions", null, savedRole.getPermissions())
                ),
                "Создана кастомная роль \"" + savedRole.getName() + "\" в проекте \"" + projectOpt.get().getName() + "\""
        );
        return ResponseEntity.ok(new RoleDto(
                savedRole.getId(),
                savedRole.getName(),
                savedRole.getIsCustom(),
                savedRole.getPermissions(),
                savedRole.getIdProject() != null ? savedRole.getIdProject().getId() : null
        ));
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<?> deleteRole(@PathVariable Long projectId, @PathVariable Long roleId, Principal principal) {
        Optional<Roles> roleOpt = rolesRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Roles role = roleOpt.get();

        if (role.getIdProject() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Эта роль не может быть изменена.");
        }

        Projects project = projectsRepository.findById(role.getIdProject().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден"));

        permissionService.checkIfProjectArchived(project);

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        boolean hasPermission = permissionService.hasPermission(user.getId(), project.getId(), "delete_roles");
        if (!hasPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        activityLogService.logActivity(
                user,
                project,
                "delete",
                "role",
                role.getId(),
                null,
                "Удалена роль \"" + role.getName() + "\" из проекта \"" + project.getName() + "\""
        );
        rolesRepository.delete(role);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{roleId}")
    @Transactional
    public ResponseEntity<?> updateRole(
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
        if (request.name == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Имя роли не может быть пустым.");
        }
        if (request.name.length() > 100) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Имя роли не может быть длиннее 100 символов.");
        }

        Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден"));

        permissionService.checkIfProjectArchived(project);

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!permissionService.hasPermission(user.getId(), projectId, "edit_roles")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        String oldName = role.getName();
        JsonNode oldPermissions = role.getPermissions();
        role.setName(request.name);
        role.setPermissions(request.permissions);

        Roles updatedRole = rolesRepository.save(role);

        List<Change> changes = new ArrayList<>();
        if (!Objects.equals(oldName, updatedRole.getName())) {
            changes.add(new Change("name", oldName, updatedRole.getName()));
        }

        if (oldPermissions == null || !oldPermissions.equals(updatedRole.getPermissions())) {
            changes.add(new Change(
                    "permissions",
                    oldPermissions != null ? oldPermissions.toPrettyString() : null,
                    updatedRole.getPermissions() != null ? updatedRole.getPermissions().toPrettyString() : null
            ));
        }

        if (!changes.isEmpty()) {
            activityLogService.logActivity(
                    user,
                    project,
                    "edit",
                    "role",
                    updatedRole.getId(),
                    changes,
                    "Изменена роль \"" + updatedRole.getName() + "\" в проекте \"" + project.getName() + "\""
            );
        }

        return ResponseEntity.ok(new RoleDto(
                updatedRole.getId(),
                updatedRole.getName(),
                updatedRole.getIsCustom(),
                updatedRole.getPermissions(),
                updatedRole.getIdProject() != null ? updatedRole.getIdProject().getId() : null
        ));
    }

}
