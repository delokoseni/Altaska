package com.example.Altaska.controller;

import com.example.Altaska.dto.Change;
import com.example.Altaska.models.*;
import com.example.Altaska.repositories.*;
import com.example.Altaska.services.ActivityLogService;
import com.example.Altaska.services.NotificationService;
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
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private TaskPerformersRepository taskPerformersRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Tasks>> getTasksByProject(@PathVariable Long projectId, Principal principal) {
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден"));

        boolean isOwner = project.getIdOwner().getId().equals(user.getId());
        boolean canViewAll = permissionService.hasPermission(user.getId(), projectId, "view_all_tasks");

        List<Tasks> tasks;
        if (isOwner || canViewAll) {
            tasks = tasksRepository.findByIdProject_Id(projectId);
        } else {
            tasks = tasksRepository.findTasksByUserOrNoPerformers(user.getId(), projectId);
        }

        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/create/{projectId}")
    public ResponseEntity<?> createTask(@PathVariable Long projectId,
                            @RequestParam String name,
                            @RequestParam(required = false) String description,
                            @RequestParam String createdAt,
                            @RequestParam String updatedAt,
                            @RequestParam(required = false) Long priorityId,
                            @RequestParam(required = false) String startTime,
                            @RequestParam(required = false) String deadline,
                            @RequestParam(required = false) List<Long> tagIds,
                            Principal principal) {
        Optional<Projects> projectOpt = projectsRepository.findById(projectId);
        Optional<Users> userOpt = usersRepository.findByEmail(principal.getName());

        if (projectOpt.isEmpty() || userOpt.isEmpty()) {
            throw new RuntimeException("Проект или пользователь не найден");
        }
        if(name == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Название задачи не может быть пустым!");
        }
        if(name.length() > 50) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Длина названия задачи не может быть больше 50 символов!");
        }

        Projects project = projectOpt.get();
        Users user = userOpt.get();

        if(permissionService.checkIfProjectArchived(project))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        if (!permissionService.hasPermission(user.getId(), projectId, "create_tasks")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }

        if (projectOpt.isPresent() && userOpt.isPresent()) {
            Tasks task = new Tasks();
            task.setName(name);
            if (description != null) {
                task.setDescription(description);
            }
            task.setIdProject(projectOpt.get());
            if(permissionService.checkIfProjectArchived(task.getIdProject()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

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

            LocalDateTime startTimeDateTime = null;
            if (startTime != null && !startTime.isEmpty()) {
                LocalDateTime localDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                ZoneId serverZoneId = ZoneId.of(TimeZone.getDefault().getID());
                ZonedDateTime zonedDateTime = localDateTime.atZone(serverZoneId);
                startTimeDateTime = zonedDateTime.toLocalDateTime();
            }
            task.setStartTimeServer(startTimeDateTime);

            LocalDateTime deadlineDateTime = null;
            if (deadline != null && !deadline.isEmpty()) {
                LocalDateTime localDateTime = LocalDateTime.parse(deadline, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                ZoneId serverZoneId = ZoneId.of(TimeZone.getDefault().getID());
                ZonedDateTime zonedDateTime = localDateTime.atZone(serverZoneId);
                deadlineDateTime = zonedDateTime.toLocalDateTime();
            }
            task.setDeadlineServer(deadlineDateTime);
            if (startTimeDateTime != null && deadlineDateTime != null && !deadlineDateTime.isAfter(startTimeDateTime)) {
                return ResponseEntity.badRequest().body("Дедлайн задачи должен быть позже старта задачи!");
            }
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

            activityLogService.logActivity(
                    user,
                    project,
                    "create",
                    "task",
                    savedTask.getId(),
                    List.of(
                            new Change("name", null, savedTask.getName()),
                            new Change("description", null, savedTask.getDescription()),
                            new Change("priority", null, savedTask.getIdPriority() != null ? savedTask.getIdPriority().getName() : null),
                            new Change("startTime", null, savedTask.getStartTimeServer() != null ? savedTask.getStartTimeServer().toString() : null),
                            new Change("deadline", null, savedTask.getDeadlineServer() != null ? savedTask.getDeadlineServer().toString() : null)
                    ),
                    "Создана задача \"" + savedTask.getName() + "\" в проекте \"" + project.getName() + "\""
            );

            return ResponseEntity.ok(savedTask);
        } else {
            throw new RuntimeException("Проект или пользователь не найден");
        }
    }

    @PutMapping("/{taskId}/name")
    public ResponseEntity<?> updateTaskName(@PathVariable Long taskId,
                                @RequestParam String name,
                                Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));
        if(name == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Название задачи не может быть пустым!");
        }
        if(name.length() > 50) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Длина названия задачи не может быть больше 50 символов!");
        }
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if(permissionService.checkIfProjectArchived(task.getIdProject()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "edit_task_title")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        String oldName = task.getName();
        task.setName(name);
        task.setUpdatedBy(principal.getName());
        task.setUpdatedAtServer(LocalDateTime.now());
        task.setUpdatedAt(OffsetDateTime.now());
        Tasks updatedTask = tasksRepository.save(task);

        if (!oldName.equals(name)) {
            activityLogService.logActivity(
                    user,
                    task.getIdProject(),
                    "edit",
                    "task",
                    updatedTask.getId(),
                    List.of(new Change("name", oldName, name)),
                    "Название задачи изменено с \"" + oldName + "\" на \"" + name + "\""
            );
            Set<Users> recipients = new HashSet<>();
            List<TaskPerformers> performers = taskPerformersRepository.findByIdTaskId(task.getId());
            for (TaskPerformers performer : performers) {
                if (!performer.getIdUser().getId().equals(user.getId())) {
                    recipients.add(performer.getIdUser());
                }
            }

            Users creator = task.getIdCreator();
            if (!creator.getId().equals(user.getId())) {
                recipients.add(creator);
            }

            if (!recipients.isEmpty()) {
                String message = "Название задачи было изменено с \"" + oldName + "\" на \"" + name + "\"";
                notificationService.notifyUsers(
                        recipients,
                        "edit_task_name",
                        "tasks",
                        updatedTask.getId(),
                        user,
                        message
                );
            }
        }
        return ResponseEntity.ok(updatedTask);
    }

    @PutMapping("/{taskId}/description")
    public ResponseEntity<?> updateTaskDescription(@PathVariable Long taskId,
                                       @RequestParam String description,
                                       Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if(permissionService.checkIfProjectArchived(task.getIdProject()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "edit_task_description")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        String oldDescription = task.getDescription();
        task.setDescription(description);
        task.setUpdatedBy(principal.getName());
        task.setUpdatedAtServer(LocalDateTime.now());
        task.setUpdatedAt(OffsetDateTime.now());

        Tasks updatedTask = tasksRepository.save(task);

        if (!Objects.equals(oldDescription, description)) {
            activityLogService.logActivity(
                    user,
                    task.getIdProject(),
                    "edit",
                    "task",
                    updatedTask.getId(),
                    List.of(new Change("description", oldDescription, description)),
                    "Описание задачи " + task.getName() + " изменено"
            );
        }

        List<TaskPerformers> performers = taskPerformersRepository.findByIdTaskId(taskId);
        Set<Users> recipients = performers.stream()
                .map(TaskPerformers::getIdUser)
                .filter(u -> !u.getId().equals(user.getId())) // исключаем текущего
                .collect(Collectors.toSet());

        Users creator = task.getIdCreator();
        if (!creator.getId().equals(user.getId())) {
            recipients.add(creator);
        }

        if (!recipients.isEmpty()) {
            String message = "Описание задачи \"" + task.getName() + "\" было изменено пользователем " + user.getEmail();
            notificationService.notifyUsers(
                    recipients,
                    "edit_task_description",
                    "tasks",
                    task.getId(),
                    user,
                    message
            );
        }

        return ResponseEntity.ok(updatedTask);
    }

    @PutMapping("/{taskId}/priority")
    public ResponseEntity<?> updateTaskPriority(@PathVariable Long taskId,
                                    @RequestParam(required = false) Long priorityId,
                                    Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "edit_task_priority")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        if(permissionService.checkIfProjectArchived(task.getIdProject()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        String oldPriorityName = task.getIdPriority() != null ? task.getIdPriority().getName() : null;

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

        Tasks updatedTask = tasksRepository.save(task);

        String newPriorityName = updatedTask.getIdPriority() != null ? updatedTask.getIdPriority().getName() : null;

        if (!Objects.equals(oldPriorityName, newPriorityName)) {
            activityLogService.logActivity(
                    user,
                    task.getIdProject(),
                    "edit",
                    "task",
                    updatedTask.getId(),
                    List.of(new Change("priority", oldPriorityName, newPriorityName)),
                    "Приоритет задачи " + task.getName() + " изменён с \"" + (oldPriorityName != null ? oldPriorityName : "нет") + "\" на \"" + (newPriorityName != null ? newPriorityName : "нет") + "\""
            );
            List<TaskPerformers> performers = taskPerformersRepository.findByIdTaskId(taskId);
            Set<Users> recipients = performers.stream()
                    .map(TaskPerformers::getIdUser)
                    .filter(u -> !u.getId().equals(user.getId()))
                    .collect(Collectors.toSet());

            Users creator = task.getIdCreator();
            if (!creator.getId().equals(user.getId())) {
                recipients.add(creator);
            }

            if (!recipients.isEmpty()) {
                String message = "Приоритет задачи \"" + task.getName() + "\" был изменён пользователем " + user.getEmail();
                notificationService.notifyUsers(
                        recipients,
                        "edit_task_priority",
                        "tasks",
                        task.getId(),
                        user,
                        message
                );
            }
        }
        return ResponseEntity.ok(updatedTask);
    }

    @PutMapping("/{taskId}/status")
    @Transactional
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long taskId,
                                  @RequestParam Long statusId,
                                  Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "edit_task_status")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        if(permissionService.checkIfProjectArchived(task.getIdProject()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        entityManager.createNativeQuery("SET LOCAL myapp.user_id = " + user.getId())
                .executeUpdate();

        String oldStatusName = task.getIdStatus() != null ? task.getIdStatus().getName() : null;

        Statuses status = statusesRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Статус не найден"));

        task.setIdStatus(status);
        task.setStatusChangeAtServer(LocalDateTime.now());
        task.setUpdatedBy(principal.getName());
        task.setUpdatedAtServer(LocalDateTime.now());
        task.setUpdatedAt(OffsetDateTime.now());

        Tasks updatedTask = tasksRepository.save(task);

        String newStatusName = updatedTask.getIdStatus() != null ? updatedTask.getIdStatus().getName() : null;

        if (!Objects.equals(oldStatusName, newStatusName)) {
            activityLogService.logActivity(
                    user,
                    task.getIdProject(),
                    "edit",
                    "task",
                    updatedTask.getId(),
                    List.of(new Change("status", oldStatusName, newStatusName)),
                    "Статус задачи " + task.getName() + " изменён с \"" + (oldStatusName != null ? oldStatusName : "нет") + "\" на \"" + (newStatusName != null ? newStatusName : "нет") + "\""
            );
            List<TaskPerformers> performers = taskPerformersRepository.findByIdTaskId(taskId);
            Set<Users> recipients = performers.stream()
                    .map(TaskPerformers::getIdUser)
                    .filter(u -> !u.getId().equals(user.getId()))
                    .collect(Collectors.toSet());

            Users creator = task.getIdCreator();
            if (!creator.getId().equals(user.getId())) {
                recipients.add(creator);
            }

            if (!recipients.isEmpty()) {
                String message = "Статус задачи \"" + task.getName() + "\" был изменён пользователем " + user.getEmail();
                notificationService.notifyUsers(
                        recipients,
                        "edit_task_status",
                        "tasks",
                        task.getId(),
                        user,
                        message
                );
            }
        }

        return ResponseEntity.ok(updatedTask);
    }

    @PutMapping("/{taskId}/priorityByName")
    public ResponseEntity<?> updateTaskPriorityByName(@PathVariable Long taskId,
                                          @RequestBody Map<String, String> requestBody,
                                          Principal principal) {
        String priorityName = requestBody.get("priorityName");

        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "edit_task_priority")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        if(permissionService.checkIfProjectArchived(task.getIdProject()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        Priorities priority = prioritiesRepository.findByName(priorityName)
                .orElseThrow(() -> new RuntimeException("Приоритет с таким названием не найден"));
        String oldPriorityName = task.getIdPriority() != null ? task.getIdPriority().getName() : null;
        task.setIdPriority(priority);

        task.setUpdatedBy(principal.getName());
        task.setUpdatedAtServer(LocalDateTime.now());
        task.setUpdatedAt(OffsetDateTime.now());
        Tasks updatedTask = tasksRepository.save(task);

        String newPriorityName = updatedTask.getIdPriority() != null ? updatedTask.getIdPriority().getName() : null;

        if (!Objects.equals(oldPriorityName, newPriorityName)) {
            activityLogService.logActivity(
                    user,
                    task.getIdProject(),
                    "edit",
                    "task",
                    updatedTask.getId(),
                    List.of(new Change("priority", oldPriorityName, newPriorityName)),
                    "Приоритет " + task.getName() + " задачи изменён с \"" + (oldPriorityName != null ? oldPriorityName : "нет") +
                            "\" на \"" + (newPriorityName != null ? newPriorityName : "нет") + "\""
            );
            List<TaskPerformers> performers = taskPerformersRepository.findByIdTaskId(taskId);
            Set<Users> recipients = performers.stream()
                    .map(TaskPerformers::getIdUser)
                    .filter(u -> !u.getId().equals(user.getId()))
                    .collect(Collectors.toSet());

            Users creator = task.getIdCreator();
            if (!creator.getId().equals(user.getId())) {
                recipients.add(creator);
            }

            if (!recipients.isEmpty()) {
                String message = "Приоритет задачи \"" + task.getName() + "\" был изменён пользователем " + user.getEmail();
                notificationService.notifyUsers(
                        recipients,
                        "edit_task_priority",
                        "tasks",
                        task.getId(),
                        user,
                        message
                );
            }
        }
        return ResponseEntity.ok(tasksRepository.save(task));
    }

    @PutMapping("/{taskId}/statusByName")
    @Transactional
    public ResponseEntity<?> updateTaskStatusByName(@PathVariable Long taskId,
                                        @RequestBody Map<String, String> requestBody,  // Принимаем данные из тела запроса
                                        Principal principal) {
        String statusName = requestBody.get("statusName");

        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "edit_task_status")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        if(permissionService.checkIfProjectArchived(task.getIdProject()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        entityManager.createNativeQuery("SET LOCAL myapp.user_id = " + user.getId())
                .executeUpdate();
        String oldStatusName = task.getIdStatus() != null ? task.getIdStatus().getName() : null;
        Statuses status = statusesRepository.findByName(statusName)
                .orElseThrow(() -> new RuntimeException("Статус с таким названием не найден"));

        task.setIdStatus(status);
        task.setStatusChangeAtServer(LocalDateTime.now());
        task.setUpdatedBy(principal.getName());
        task.setUpdatedAtServer(LocalDateTime.now());
        task.setUpdatedAt(OffsetDateTime.now());

        Tasks updatedTask = tasksRepository.save(task);

        String newStatusName = updatedTask.getIdStatus() != null ? updatedTask.getIdStatus().getName() : null;

        if (!Objects.equals(oldStatusName, newStatusName)) {
            activityLogService.logActivity(
                    user,
                    task.getIdProject(),
                    "edit",
                    "task",
                    updatedTask.getId(),
                    List.of(new Change("status", oldStatusName, newStatusName)),
                    "Статус задачи " + task.getName() + " изменён с \"" + (oldStatusName != null ? oldStatusName : "нет") +
                            "\" на \"" + (newStatusName != null ? newStatusName : "нет") + "\""
            );
            List<TaskPerformers> performers = taskPerformersRepository.findByIdTaskId(taskId);
            Set<Users> recipients = performers.stream()
                    .map(TaskPerformers::getIdUser)
                    .filter(u -> !u.getId().equals(user.getId()))
                    .collect(Collectors.toSet());

            Users creator = task.getIdCreator();
            if (!creator.getId().equals(user.getId())) {
                recipients.add(creator);
            }

            if (!recipients.isEmpty()) {
                String message = "Статус задачи \"" + task.getName() + "\" был изменён пользователем " + user.getEmail();
                notificationService.notifyUsers(
                        recipients,
                        "edit_task_status",
                        "tasks",
                        task.getId(),
                        user,
                        message
                );
            }
        }
        return ResponseEntity.ok(tasksRepository.save(task));
    }

    @PutMapping("/{taskId}/deadline")
    public ResponseEntity<?> updateTaskDeadline(@PathVariable Long taskId,
                                    @RequestParam(required = false) String deadline,
                                    Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if(permissionService.checkIfProjectArchived(task.getIdProject()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "edit_task_deadline")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        String oldDeadline = task.getDeadlineServer() != null
                ? task.getDeadlineServer().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : null;

        if (deadline != null && !deadline.isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.parse(deadline, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            ZoneId serverZoneId = ZoneId.of(TimeZone.getDefault().getID());
            ZonedDateTime zonedDateTime = localDateTime.atZone(serverZoneId);
            task.setDeadlineServer(zonedDateTime.toLocalDateTime());
        } else {
            task.setDeadlineServer(null);
        }
        if (task.getStartTimeServer() != null && task.getDeadlineServer() != null &&
                !task.getDeadlineServer().isAfter(task.getStartTimeServer())) {
            return ResponseEntity.badRequest().body("Дедлайн задачи должен быть позже старта задачи!");
        }
        task.setUpdatedBy(principal.getName());
        task.setUpdatedAtServer(LocalDateTime.now());
        task.setUpdatedAt(OffsetDateTime.now());
        Tasks updatedTask = tasksRepository.save(task);
        String newDeadline = updatedTask.getDeadlineServer() != null
                ? updatedTask.getDeadlineServer().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : null;
        if (!Objects.equals(oldDeadline, newDeadline)) {
            activityLogService.logActivity(
                    user,
                    task.getIdProject(),
                    "edit",
                    "task",
                    updatedTask.getId(),
                    List.of(new Change("deadline", oldDeadline, newDeadline)),
                    "Срок задачи \"" + task.getName() + "\" изменён с \"" +
                            (oldDeadline != null ? oldDeadline : "не задан") + "\" на \"" +
                            (newDeadline != null ? newDeadline : "не задан") + "\""
            );
            List<TaskPerformers> performers = taskPerformersRepository.findByIdTaskId(taskId);
            Set<Users> recipients = performers.stream()
                    .map(TaskPerformers::getIdUser)
                    .filter(u -> !u.getId().equals(user.getId()))
                    .collect(Collectors.toSet());

            Users creator = task.getIdCreator();
            if (!creator.getId().equals(user.getId())) {
                recipients.add(creator);
            }

            if (!recipients.isEmpty()) {
                String message = "Срок задачи \"" + task.getName() + "\" был изменён пользователем " + user.getEmail();
                notificationService.notifyUsers(
                        recipients,
                        "edit_task_deadline",
                        "tasks",
                        task.getId(),
                        user,
                        message
                );
            }
        }
        return ResponseEntity.ok(updatedTask);
    }

    @PutMapping("/{taskId}/startTime")
    public ResponseEntity<?> updateTaskStartTime(@PathVariable Long taskId,
                                                 @RequestParam(required = false) String startTime,
                                                 Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if(permissionService.checkIfProjectArchived(task.getIdProject()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "edit_task_start_date")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }

        String oldStartTime = task.getStartTimeServer() != null
                ? task.getStartTimeServer().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : null;

        if (startTime != null && !startTime.isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            ZoneId serverZoneId = ZoneId.of(TimeZone.getDefault().getID());
            ZonedDateTime zonedDateTime = localDateTime.atZone(serverZoneId);
            task.setStartTimeServer(zonedDateTime.toLocalDateTime());
        } else {
            task.setStartTimeServer(null);
        }
        if (task.getStartTimeServer() != null && task.getDeadlineServer() != null &&
                !task.getDeadlineServer().isAfter(task.getStartTimeServer())) {
            return ResponseEntity.badRequest().body("Дедлайн задачи должен быть позже старта задачи!");
        }
        task.setUpdatedBy(principal.getName());
        task.setUpdatedAtServer(LocalDateTime.now());
        task.setUpdatedAt(OffsetDateTime.now());

        Tasks updatedTask = tasksRepository.save(task);

        String newStartTime = updatedTask.getStartTimeServer() != null
                ? updatedTask.getStartTimeServer().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : null;

        if (!Objects.equals(oldStartTime, newStartTime)) {
            activityLogService.logActivity(
                    user,
                    task.getIdProject(),
                    "edit",
                    "task",
                    updatedTask.getId(),
                    List.of(new Change("start_time", oldStartTime, newStartTime)),
                    "Дата начала задачи \"" + task.getName() + "\" изменена с \"" +
                            (oldStartTime != null ? oldStartTime : "не задана") + "\" на \"" +
                            (newStartTime != null ? newStartTime : "не задана") + "\""
            );

            List<TaskPerformers> performers = taskPerformersRepository.findByIdTaskId(taskId);
            Set<Users> recipients = performers.stream()
                    .map(TaskPerformers::getIdUser)
                    .filter(u -> !u.getId().equals(user.getId()))
                    .collect(Collectors.toSet());

            Users creator = task.getIdCreator();
            if (!creator.getId().equals(user.getId())) {
                recipients.add(creator);
            }

            if (!recipients.isEmpty()) {
                String message = "Дата начала задачи \"" + task.getName() + "\" была изменена пользователем " + user.getEmail();
                notificationService.notifyUsers(
                        recipients,
                        "edit_task_start_time",
                        "tasks",
                        task.getId(),
                        user,
                        message
                );
            }
        }

        return ResponseEntity.ok(updatedTask);
    }

    @Transactional
    @DeleteMapping("/{taskId}/delete")
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId, Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if(permissionService.checkIfProjectArchived(task.getIdProject()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Проект архивирован и не может быть изменён");

        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "delete_tasks")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }

        List<TaskPerformers> performers = taskPerformersRepository.findByIdTaskId(taskId);
        Set<Users> recipients = performers.stream()
                .map(TaskPerformers::getIdUser)
                .filter(u -> !u.getId().equals(user.getId()))
                .collect(Collectors.toSet());

        Users creator = task.getIdCreator();
        if (!creator.getId().equals(user.getId())) {
            recipients.add(creator);
        }

        String taskName = task.getName();
        if (!recipients.isEmpty()) {
            String message = "Задача \"" + taskName + "\" была удалена пользователем " + user.getEmail();
            notificationService.notifyUsers(
                    recipients,
                    "delete_task",
                    "tasks",
                    taskId,
                    user,
                    message
            );
        }

        activityLogService.logActivity(
                user,
                task.getIdProject(),
                "delete",
                "task",
                taskId,
                null,
                "Задача \"" + taskName + "\" была удалена"
        );

        taskCleanupService.deleteTaskDependencies(taskId);
        taskCleanupService.deleteComments(taskId);
        taskCleanupService.deleteSubtasks(taskId);
        tasksRepository.delete(task);

        return ResponseEntity.ok(Map.of("message", "Задача успешно удалена"));
    }


    @GetMapping("/project/{projectId}/withperformers")
    public List<Map<String, Object>> getTasksWithPerformersByProject(@PathVariable Long projectId, Principal principal) {
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Projects projectFind = projectsRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден"));

        boolean isOwner = projectFind.getIdOwner().getId().equals(user.getId());
        boolean canViewAll = permissionService.hasPermission(user.getId(), projectId, "view_all_tasks");
        List<Tasks> tasks;
        if (isOwner || canViewAll) {
            tasks = tasksRepository.findByIdProject_Id(projectId);
        }
        else {
            tasks = tasksRepository.findTasksByUserOrNoPerformers(user.getId(), projectId);
        }
        List<Map<String, Object>> result = new ArrayList<>();

        for (Tasks task : tasks) {
            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("id", task.getId());
            taskMap.put("name", task.getName());
            taskMap.put("description", task.getDescription());
            taskMap.put("createdAt", task.getCreatedAt());
            taskMap.put("updatedAt", task.getUpdatedAt());
            taskMap.put("updatedBy", task.getUpdatedBy());
            taskMap.put("timeSpent", task.getTimeSpent());
            taskMap.put("deadlineServer", task.getDeadlineServer());
            taskMap.put("statusChangeAtServer", task.getStatusChangeAtServer());

            if (task.getIdStatus() != null) {
                Map<String, Object> status = new HashMap<>();
                status.put("id", task.getIdStatus().getId());
                status.put("name", task.getIdStatus().getName());
                taskMap.put("idStatus", status);
            }

            if (task.getIdPriority() != null) {
                Map<String, Object> priority = new HashMap<>();
                priority.put("id", task.getIdPriority().getId());
                priority.put("name", task.getIdPriority().getName());
                priority.put("level", task.getIdPriority().getLevel());
                taskMap.put("idPriority", priority);
            }

            if (task.getIdProject() != null) {
                Map<String, Object> project = new HashMap<>();
                project.put("id", task.getIdProject().getId());
                taskMap.put("idProject", project);
            }

            List<TaskPerformers> performers = taskPerformersRepository.findByIdTaskId(task.getId());
            List<Map<String, Object>> performerList = performers.stream().map(tp -> {
                Map<String, Object> p = new HashMap<>();
                p.put("id", tp.getIdUser().getId());
                p.put("email", tp.getIdUser().getEmail());
                return p;
            }).collect(Collectors.toList());

            taskMap.put("performers", performerList);

            result.add(taskMap);
        }

        return result;
    }

    @GetMapping("/project/dto/{projectId}")
    public List<TaskDto> getAllTasksForProject(@PathVariable Long projectId) {
        List<Tasks> tasks = tasksRepository.findByIdProject_Id(projectId);

        return tasks.stream().map(task -> {
            List<TaskPerformers> performers = taskPerformersRepository.findByIdTaskId(task.getId());

            List<PerformerDto> performerDtos = performers.stream()
                    .map(tp -> new PerformerDto(tp.getIdUser().getId(), tp.getIdUser().getEmail()))
                    .collect(Collectors.toList());

            List<String> tagNames = tasksTagsRepository.findByIdTask(task)
                    .stream()
                    .map(tasksTags -> tasksTags.getIdTag().getName())
                    .toList();

            return new TaskDto(task, performerDtos, tagNames);
        }).collect(Collectors.toList());
    }

    public class PerformerDto {
        private Long userId;
        private String email;

        public PerformerDto(Long userId, String email) {
            this.userId = userId;
            this.email = email;
        }

        public Long getUserId() { return userId; }
        public String getEmail() { return email; }
        public void setUserId(Long userId) { this.userId = userId; }
        public void setEmail(String email) { this.email = email; }
    }

    public class ProjectDto {
        private Long id;
        public ProjectDto(Long id) {
            this.id = id;
        }

        public Long getId() { return id; }
    }

    public class TaskDto {
        private Long id;
        private String name;
        private String description;
        private String status; // status.name
        private String priority; // priority.name
        private List<PerformerDto> performers;
        private List<String> tags;
        private ProjectDto idProject; // Используем ProjectDto

        public TaskDto(Tasks task, List<PerformerDto> performers, List<String> tags) {
            this.id = task.getId();
            this.name = task.getName();
            this.description = task.getDescription();
            this.status = task.getIdStatus() != null ? task.getIdStatus().getName() : "Без статуса";
            this.priority = task.getIdPriority() != null ? task.getIdPriority().getName() : "Без приоритета";
            this.performers = performers;
            this.tags = tags;
            this.idProject = new ProjectDto(task.getIdProject().getId()); // Передаем ProjectDto с id
        }

        // Геттеры
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public String getPriority() { return priority; }
        public List<PerformerDto> getPerformers() { return performers; }
        public ProjectDto getIdProject() { return idProject; } // Возвращаем объект ProjectDto
    }

    @GetMapping("/project/{projectId}/performers-map")
    public ResponseEntity<Map<Long, List<String>>> getPerformersMap(@PathVariable Long projectId) {
        // Получаем все задачи проекта
        List<Tasks> projectTasks = tasksRepository.findByIdProject_Id(projectId);
        List<Long> taskIds = projectTasks.stream()
                .map(Tasks::getId)
                .toList();

        // Получаем исполнителей по задачам
        List<TaskPerformers> performers = taskPerformersRepository.findByIdTaskIdIn(taskIds);

        // Группируем по taskId
        Map<Long, List<String>> result = performers.stream()
                .collect(Collectors.groupingBy(
                        tp -> tp.getIdTask().getId(),
                        Collectors.mapping(tp -> tp.getIdUser().getEmail(), Collectors.toList())
                ));

        return ResponseEntity.ok(result);
    }

    @GetMapping("/assigned")
    public List<Tasks> getAssignedTasks(Principal principal) {
        return tasksRepository.findAllByPerformer(principal.getName());
    }

}

