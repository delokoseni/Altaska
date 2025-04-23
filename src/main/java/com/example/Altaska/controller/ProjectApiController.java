package com.example.Altaska.controller;

import com.example.Altaska.models.ProjectMembers;
import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Roles;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.ProjectMembersRepository;
import com.example.Altaska.repositories.ProjectsRepository;
import com.example.Altaska.repositories.RolesRepository;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.services.EmailService;
import com.example.Altaska.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
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

    @Autowired
    private EmailService emailService;

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

    @PostMapping("/{projectId}/invite")
    public ResponseEntity<?> inviteMember(@PathVariable Long projectId,
                                          @RequestParam String email,
                                          @RequestParam Long roleId,
                                          Principal principal) {
        Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));

        Users inviter = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!permissionService.hasPermission(inviter.getId(), projectId, "invite")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Нет прав для приглашения");
        }

        permissionService.checkIfProjectArchived(project);

        if (email.equalsIgnoreCase(project.getIdOwner().getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Владелец проекта не может быть приглашён.");
        }

        // Проверка: не является ли пользователь уже участником проекта
        boolean alreadyMember = projectMembersRepository.existsByIdProjectAndInviteeEmail(project, email);
        if (alreadyMember) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Пользователь уже является участником проекта.");
        }

        Optional<Users> optionalInvitee = usersRepository.findByEmail(email);
        if (optionalInvitee.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Приглашаемый пользователь не найден");
        }
        Users invitee = optionalInvitee.get();


        Roles role = rolesRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Роль не найдена"));

        // Генерация токена
        String token = UUID.randomUUID().toString();

        // Создание участника проекта
        ProjectMembers member = new ProjectMembers();
        member.setIdProject(project);
        member.setIdUser(invitee);
        member.setIdRole(role);
        member.setConfirmed(false);
        member.setConfirmationToken(token);
        member.setAddedAt(LocalDate.now()); //TODO Заменить на время клиента
        member.setAddedAtServer(LocalDate.now());
        member.setInvitedBy(inviter.getEmail());
        member.setInviteeEmail(email);
        projectMembersRepository.save(member);

        // Формирование и отправка письма
        String link = "http://localhost:8080/api/projects/confirm-invite?token=" + token;
        String subject = "Приглашение в проект";
        String body = "Вас пригласили в проект \"" + project.getName() + "\". Подтвердите участие, перейдя по ссылке:\n" + link;

        emailService.sendEmail(email, subject, body);

        return ResponseEntity.ok("Приглашение отправлено");
    }


    @GetMapping("/confirm-invite")
    public ResponseEntity<?> confirmInvite(@RequestParam String token) {
        Optional<ProjectMembers> memberOpt = projectMembersRepository.findByConfirmationToken(token);

        if (memberOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Недействительный токен");
        }

        ProjectMembers member = memberOpt.get();
        member.setConfirmed(true);
        member.setConfirmationToken(null);
        projectMembersRepository.save(member);

        return ResponseEntity.ok("Участие в проекте подтверждено");
    }
}

