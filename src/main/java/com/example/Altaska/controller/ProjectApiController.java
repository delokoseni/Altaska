package com.example.Altaska.controller;

import com.example.Altaska.dto.Change;
import com.example.Altaska.models.ProjectMembers;
import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Roles;
import com.example.Altaska.models.Users;
import com.example.Altaska.models.Tasks;
import com.example.Altaska.repositories.*;
import com.example.Altaska.services.*;
import com.example.Altaska.validators.EmailValidator;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private TasksTagsRepository tasksTagsRepository;

    @Autowired
    private TagsRepository tagsRepository;

    @Autowired
    private TaskCleanupService taskCleanupService;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private NotificationService notificationService;

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
        if (!permissionService.hasPermission(user.getId(), project.getId(), "edit_project_title_description")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        if(permissionService.checkIfProjectArchived(project))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        List<Change> changes = new ArrayList<>();
        if (name != null && !name.equals(project.getName())) {
            changes.add(new Change("name", project.getName(), name));
            project.setName(name);
        }
        if ((description != null ? description.length() : 0) >  1500) {
            return ResponseEntity.badRequest().body("Описание проекта не может быть длиннее 1500 символов!");
        }
        if (description != null && !description.equals(project.getDescription())) {
            changes.add(new Change("description", project.getDescription(), description));
            project.setDescription(description);
        }
        if (name != null && name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Имя проекта не может быть пустым!");
        }
        if (name != null && name.length() >  128) {
            return ResponseEntity.badRequest().body("Имя проекта не может быть длиннее 128 символов!");
        }
        projectsRepository.save(project);
        if (!changes.isEmpty()) {
            activityLogService.logActivity(
                    user,
                    project,
                    "update",
                    "project",
                    project.getId(),
                    changes,
                    "Обновлены поля проекта"
            );
        }
        return ResponseEntity.ok(project);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<?> getProjectMembers(@PathVariable Long id) {
        List<ProjectMembers> members = projectMembersRepository.findByIdProjectId(id);

        List<Map<String, Object>> result = members.stream().map(member -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", member.getIdUser().getId());
            map.put("email", member.getIdUser().getEmail());
            map.put("confirmed", member.getConfirmed());

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

    @GetMapping("/{id}/confirmed-members")
    public ResponseEntity<?> getConfirmedMembersWithOwner(@PathVariable Long id) {
        Projects project = projectsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден"));

        List<ProjectMembers> members = projectMembersRepository.findByIdProjectIdAndConfirmedTrue(id);

        Set<Users> users = members.stream()
                .map(ProjectMembers::getIdUser)
                .collect(Collectors.toSet());
        users.add(project.getIdOwner());

        List<Map<String, Object>> result = users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", user.getId());
            map.put("email", user.getEmail());
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

        if (!permissionService.hasPermission(user.getId(), project.getId(), "archive_project")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }

        boolean newStatus = payload.getOrDefault("archived", false);
        boolean oldStatus = project.getIsArchived();

        if (newStatus != oldStatus) {
            List<Change> changes = List.of(new Change("archived", oldStatus, newStatus));
            project.setIsArchived(newStatus);
            projectsRepository.save(project);

            activityLogService.logActivity(
                    user,
                    project,
                    "update",
                    "project",
                    project.getId(),
                    changes,
                    newStatus ? "Проект был архивирован" : "Проект был разархивирован"
            );
        }
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

        if (Objects.equals(currentUser.getId(), userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы не можете поменять свою роль.");
        }

        if (!permissionService.hasPermission(currentUser.getId(), project.getId(), "change_user_roles")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }

        if(permissionService.checkIfProjectArchived(project))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        ProjectMembers member = projectMembersRepository.findByIdProjectIdAndIdUserId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("Участник не найден"));

        Roles oldRole = member.getIdRole();

        Roles newRole = rolesRepository.findById(newRoleId)
                .orElseThrow(() -> new RuntimeException("Роль не найдена"));

        if (!oldRole.getId().equals(newRole.getId())) {
            member.setIdRole(newRole);
            projectMembersRepository.save(member);

            List<Change> changes = List.of(new Change(
                    "role",
                    oldRole.getName(),
                    newRole.getName()
            ));

            activityLogService.logActivity(
                    currentUser,
                    project,
                    "update",
                    "project_member",
                    member.getIdUser().getId(),
                    changes,
                    String.format("Изменена роль участника %s", member.getIdUser().getEmail())
            );
            String message = String.format(
                    "Ваша роль в проекте \"%s\" была изменена с \"%s\" на \"%s\" пользователем %s",
                    project.getName(),
                    oldRole.getName(),
                    newRole.getName(),
                    currentUser.getEmail()
            );

            notificationService.notifyUsers(
                    Set.of(member.getIdUser()),
                    "change_role",
                    "projects",
                    projectId,
                    currentUser,
                    message
            );
        }

        return ResponseEntity.ok(Map.of("message", "Роль обновлена"));

    }

    @PostMapping("/{projectId}/invite")
    public ResponseEntity<?> inviteMember(@PathVariable Long projectId,
                                          @RequestParam String email,
                                          @RequestParam Long roleId,
                                          @RequestParam String clientDate,
                                          Principal principal) {

        if (!EmailValidator.isValidLength(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Превышена максимальная длина электронной почты: " + EmailValidator.getMaxEmailLength());
        }

        if (!EmailValidator.isValidFormat(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Неверный формат электронной почты.");
        }

        Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));

        Users inviter = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!permissionService.hasPermission(inviter.getId(), projectId, "invite_project_members")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }

        if(permissionService.checkIfProjectArchived(project))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        if (email.equalsIgnoreCase(project.getIdOwner().getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Владелец проекта не может быть приглашён.");
        }

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

        String token = UUID.randomUUID().toString();

        ProjectMembers member = new ProjectMembers();
        member.setIdProject(project);
        member.setIdUser(invitee);
        member.setIdRole(role);
        member.setConfirmed(false);
        member.setConfirmationToken(token);
        member.setAddedAt(LocalDate.parse(clientDate));
        member.setAddedAtServer(LocalDate.now());
        member.setInvitedBy(inviter.getEmail());
        member.setInviteeEmail(email);
        projectMembersRepository.save(member);

        String link = "http://localhost:8080/confirm-invite?token=" + token;
        String subject = "Приглашение в проект";
        String body = "Вас пригласили в проект \"" + project.getName() + "\". Подтвердите участие, перейдя по ссылке:\n" + link;

        emailService.sendEmail(email, subject, body);
        activityLogService.logActivity(
                inviter,
                project,
                "create",
                "project_member",
                invitee.getId(),
                List.of(new Change(
                        "invitation",
                        null,
                        "Приглашён пользователь с email " + email + " с ролью " + role.getName()
                )),
                "Пользователь " + email + " был приглашён в проект"
        );
        return ResponseEntity.ok(Map.of("message", "Приглашение отправлено"));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable Long projectId,
                                          @PathVariable Long userId,
                                          Principal principal) {
        Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));

        Users currentUser = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!permissionService.hasPermission(currentUser.getId(), projectId, "remove_project_members")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }

        if(permissionService.checkIfProjectArchived(project))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        Optional<ProjectMembers> memberOpt = projectMembersRepository.findByIdProjectIdAndIdUserId(projectId, userId);
        if (memberOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Участник не найден");
        }

        projectMembersRepository.delete(memberOpt.get());
        Users removedUser = memberOpt.get().getIdUser();

        activityLogService.logActivity(
                currentUser,
                project,
                "delete",
                "project_member",
                removedUser.getId(),
                List.of(
                        new Change("deleted", false, true)
                ),
                "Пользователь " + removedUser.getEmail() + " был удалён из проекта"
        );

        return ResponseEntity.ok(Map.of("message", "Участник удалён"));
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id, Principal principal) {
        Projects project = projectsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!permissionService.hasPermission(user.getId(), project.getId(), "delete_project")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }

        if(permissionService.checkIfProjectArchived(project))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        List<Tasks> tasks = tasksRepository.findByIdProject_Id(id);
        for (Tasks task : tasks) {
            taskCleanupService.deleteTaskDependencies(task.getId());
            taskCleanupService.deleteComments(task.getId());
            taskCleanupService.deleteSubtasks(task.getId());
            tasksRepository.delete(task);
        }

        tagsRepository.deleteAll(tagsRepository.findByIdProjectId(id));
        projectMembersRepository.deleteAll(projectMembersRepository.findByIdProjectId(project.getId()));
        List<Roles> roles = rolesRepository.findByIdProject_IdOrIdProjectIsNull(id);
        roles.removeIf(role -> role.getIdProject() == null);
        rolesRepository.deleteAll(roles);
        activityLogRepository.deleteByIdProject_Id(project.getId());

        projectsRepository.delete(project);

        return ResponseEntity.ok(Map.of("message", "Проект успешно удалён"));
    }

    @DeleteMapping("/{projectId}/members/leave")
    public ResponseEntity<?> leaveProject(@PathVariable Long projectId, Principal principal) {
        Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));
        Users currentUser = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (Objects.equals(project.getIdOwner().getId(), currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Владелец не может покинуть проект."));
        }
        if(permissionService.checkIfProjectArchived(project))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        Optional<ProjectMembers> memberOpt = projectMembersRepository.findByIdProjectIdAndIdUserId(projectId, currentUser.getId());
        if (memberOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Вы не являетесь участником этого проекта"));
        }
        projectMembersRepository.delete(memberOpt.get());
        activityLogService.logActivity(
                currentUser,
                project,
                "delete",
                "project_member",
                currentUser.getId(),
                List.of(new Change("deleted", false, true)),
                "Пользователь " + currentUser.getEmail() + " покинул проект"
        );
        String message = String.format(
                "Пользователь \"%s\" покинул проект \"%s\".",
                currentUser.getEmail(),
                project.getName()
        );

        notificationService.notifyUsers(
                Set.of(project.getIdOwner()),
                "leave_user",
                "projects",
                projectId,
                currentUser,
                message
        );

        return ResponseEntity.ok(Map.of("message", "Вы покинули проект"));
    }

    @GetMapping("/{projectId}/owner")
    public ResponseEntity<String> getProjectOwnerEmail(@PathVariable Long projectId) {
        Optional<Projects> projectOpt = projectsRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Users owner = projectOpt.get().getIdOwner();
        if (owner == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(owner.getEmail());
    }

}

