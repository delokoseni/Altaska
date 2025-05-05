package com.example.Altaska.controller;

import com.example.Altaska.models.*;
import com.example.Altaska.repositories.*;
import com.example.Altaska.services.PermissionService;
import com.example.Altaska.services.TaskCleanupService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

@RestController
@RequestMapping("/api/tasks")
public class TaskApiController {

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ProjectsRepository projectsRepository;

    @Autowired
    private StatusesRepository statusesRepository;

    @Autowired
    private PrioritiesRepository prioritiesRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private  TasksTagsRepository tasksTagsRepository;

    @Autowired
    private TagsRepository tagsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TaskCleanupService taskCleanupService;

    @GetMapping("/project/{projectId}")
    public List<Tasks> getTasksByProject(@PathVariable Long projectId) {
        return tasksRepository.findByIdProject_Id(projectId);
    }

    @PostMapping("/create/{projectId}")
    public Tasks createTask(@PathVariable Long projectId,
                            @RequestParam String name,
                            @RequestParam(required = false) String description,
                            @RequestParam String createdAt,
                            @RequestParam String updatedAt,
                            @RequestParam(required = false) Long priorityId,
                            @RequestParam(required = false) String deadline,
                            @RequestParam(required = false) List<Long> tagIds,
                            Principal principal) {
        Optional<Projects> projectOpt = projectsRepository.findById(projectId);
        Optional<Users> userOpt = usersRepository.findByEmail(principal.getName());

        if (projectOpt.isPresent() && userOpt.isPresent()) {
            Tasks task = new Tasks();
            task.setName(name);
            if (description != null) {
                task.setDescription(description);
            }
            task.setIdProject(projectOpt.get());
            permissionService.checkIfProjectArchived(task.getIdProject());
            task.setIdCreator(userOpt.get());
            if (priorityId != null) {
                Priorities priority = prioritiesRepository.findById(priorityId)
                        .orElseThrow(() -> new RuntimeException("Приоритет не найден"));
                task.setIdPriority(priority);
            }

            LocalDate nowDate = LocalDate.now();
            LocalDateTime nowDateTime = LocalDateTime.now();
            LocalDate createdAtDate = LocalDate.parse(createdAt);
            OffsetDateTime updatedAtOffset = OffsetDateTime.parse(updatedAt);
            task.setCreatedAt(createdAtDate);
            task.setUpdatedAt(updatedAtOffset);
            task.setCreatedAtServer(nowDate);
            task.setUpdatedAtServer(nowDateTime);
            task.setStatusChangeAtServer(nowDateTime);
            task.setUpdatedBy(principal.getName());
            task.setTimeSpent(0L);
            Statuses status = statusesRepository.findById(1L).orElseThrow(() -> new RuntimeException("Статус не найден"));
            task.setIdStatus(status);
            LocalDateTime deadlineDateTime = null;
            if (deadline != null && !deadline.isEmpty()) {
                LocalDateTime localDateTime = LocalDateTime.parse(deadline, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                ZoneId serverZoneId = ZoneId.of(TimeZone.getDefault().getID());
                ZonedDateTime zonedDateTime = localDateTime.atZone(serverZoneId);
                deadlineDateTime = zonedDateTime.toLocalDateTime();
            }
            task.setDeadlineServer(deadlineDateTime);
            Tasks savedTask = tasksRepository.save(task);

            if (tagIds != null) {
                for (Long tagId : tagIds) {
                    Tags tag = tagsRepository.findById(tagId)
                            .orElseThrow(() -> new RuntimeException("Тег не найден"));
                    TasksTags tasksTags = new TasksTags();
                    tasksTags.setIdTask(savedTask);
                    tasksTags.setIdTag(tag);
                    tasksTags.setAddedAtServer(LocalDateTime.now());
                    tasksTagsRepository.save(tasksTags);
                }
            }

            return savedTask;
        } else {
            throw new RuntimeException("Проект или пользователь не найден");
        }
    }

    @PutMapping("/{taskId}/name")
    public Tasks updateTaskName(@PathVariable Long taskId,
                                @RequestParam String name,
                                Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        permissionService.checkIfProjectArchived(task.getIdProject());
        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "edit")) {
            throw new RuntimeException("Нет доступа");
        }

        task.setName(name);
        task.setUpdatedBy(principal.getName());
        task.setUpdatedAtServer(LocalDateTime.now());
        task.setUpdatedAt(OffsetDateTime.now());
        return tasksRepository.save(task);
    }

    @PutMapping("/{taskId}/description")
    public Tasks updateTaskDescription(@PathVariable Long taskId,
                                       @RequestParam String description,
                                       Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        permissionService.checkIfProjectArchived(task.getIdProject());
        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "edit")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа");
        }

        task.setDescription(description);
        task.setUpdatedBy(principal.getName());
        task.setUpdatedAtServer(LocalDateTime.now());
        task.setUpdatedAt(OffsetDateTime.now());
        return tasksRepository.save(task);
    }

    @PutMapping("/{taskId}/priority")
    public Tasks updateTaskPriority(@PathVariable Long taskId,
                                    @RequestParam(required = false) Long priorityId,
                                    Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "edit")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа");
        }
        permissionService.checkIfProjectArchived(task.getIdProject());
        if (priorityId != null) {
            Priorities priority = prioritiesRepository.findById(priorityId)
                    .orElseThrow(() -> new RuntimeException("Приоритет не найден"));
            task.setIdPriority(priority);
        } else {
            task.setIdPriority(null);
        }

        task.setUpdatedBy(principal.getName());
        task.setUpdatedAtServer(LocalDateTime.now());
        task.setUpdatedAt(OffsetDateTime.now());
        return tasksRepository.save(task);
    }

    @PutMapping("/{taskId}/status")
    @Transactional
    public Tasks updateTaskStatus(@PathVariable Long taskId,
                                  @RequestParam Long statusId,
                                  Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "edit")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа");
        }
        permissionService.checkIfProjectArchived(task.getIdProject());

        entityManager.createNativeQuery("SET LOCAL myapp.user_id = " + user.getId())
                .executeUpdate();


        Statuses status = statusesRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Статус не найден"));

        task.setIdStatus(status);
        task.setStatusChangeAtServer(LocalDateTime.now());
        task.setUpdatedBy(principal.getName());
        task.setUpdatedAtServer(LocalDateTime.now());
        task.setUpdatedAt(OffsetDateTime.now());
        return tasksRepository.save(task);
    }

    @PutMapping("/{taskId}/deadline")
    public Tasks updateTaskDeadline(@PathVariable Long taskId,
                                    @RequestParam(required = false) String deadline,
                                    Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        permissionService.checkIfProjectArchived(task.getIdProject());
        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "edit")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа");
        }

        if (deadline != null && !deadline.isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.parse(deadline, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            ZoneId serverZoneId = ZoneId.of(TimeZone.getDefault().getID());
            ZonedDateTime zonedDateTime = localDateTime.atZone(serverZoneId);
            task.setDeadlineServer(zonedDateTime.toLocalDateTime());
        } else {
            task.setDeadlineServer(null);
        }

        task.setUpdatedBy(principal.getName());
        task.setUpdatedAtServer(LocalDateTime.now());
        task.setUpdatedAt(OffsetDateTime.now());
        return tasksRepository.save(task);
    }

    @DeleteMapping("/{taskId}/delete")
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId, Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        permissionService.checkIfProjectArchived(task.getIdProject());
        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "delete")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа");
        }

        taskCleanupService.deleteTaskDependencies(taskId);
        taskCleanupService.deleteComments(taskId);
        taskCleanupService.deleteSubtasks(taskId);

        tasksRepository.delete(task);

        // Отправляем JSON-ответ
        return ResponseEntity.ok(Map.of("message", "Задача успешно удалена"));
    }

    // API эндпоинт для получения всех задач для проекта
    @GetMapping("/api/tasks/project/{projectId}")
    public List<Tasks> getAllTasksForProject(@PathVariable Long projectId) {
        return tasksRepository.findByIdProject_Id(projectId);
    }


}

