package com.example.Altaska.controller;

import com.example.Altaska.dto.Change;
import com.example.Altaska.services.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.Altaska.repositories.TasksRepository;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.repositories.SubTasksRepository;
import com.example.Altaska.services.PermissionService;
import com.example.Altaska.models.SubTasks;
import com.example.Altaska.models.Tasks;
import com.example.Altaska.models.Users;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/subtasks")
public class SubTaskApiController {

    @Autowired
    private SubTasksRepository subTasksRepository;

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ActivityLogService activityLogService;

    @PostMapping
    public ResponseEntity<?> createSubtasks(@PathVariable Long taskId,
                                         @RequestBody List<SubTaskDTO> subtasks,
                                         Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        permissionService.checkIfProjectArchived(task.getIdProject());

        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "create_subtasks")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }

        List<SubTasks> saved = new ArrayList<>();
        for (SubTaskDTO dto : subtasks) {
            SubTasks subTask = new SubTasks();
            subTask.setIdTask(task);
            subTask.setName(dto.getName());
            subTask.setDescription(dto.getDescription());
            subTask.setCreatedAt(LocalDate.now()); //TODO заменить на время пользователя
            subTask.setCreatedAtServer(LocalDate.now());
            SubTasks savedSubTask = subTasksRepository.save(subTask);
            saved.add(savedSubTask);
            activityLogService.logActivity(
                    user,
                    task.getIdProject(),
                    "create",
                    "sub_task",
                    savedSubTask.getId(),
                    List.of(
                            new Change("name", null, savedSubTask.getName()),
                            new Change("description", null, savedSubTask.getDescription())
                    ),
                    "Создана подзадача \"" + savedSubTask.getName() + "\" для задачи \"" + task.getName() + "\""
            );
        }

        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<SubTasks> getSubtasks(@PathVariable Long taskId, Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        return subTasksRepository.findByIdTask_Id(taskId);
    }

    public static class SubTaskDTO {
        private String name;
        private String description;

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    @PutMapping("/{subtaskId}")
    public ResponseEntity<?> updateSubtask(@PathVariable Long subtaskId,
                                  @RequestBody SubTaskDTO dto,
                                  Principal principal) {
        SubTasks subTask = subTasksRepository.findById(subtaskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Подзадача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        permissionService.checkIfProjectArchived(subTask.getIdTask().getIdProject());

        if (!permissionService.hasPermission(user.getId(), subTask.getIdTask().getIdProject().getId(), "edit_subtasks")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }

        String oldName = subTask.getName();
        String oldDescription = subTask.getDescription();
        subTask.setName(dto.getName());
        subTask.setDescription(dto.getDescription());
        SubTasks updatedSubTask = subTasksRepository.save(subTask);

        // Формируем список изменений
        List<Change> changes = new ArrayList<>();
        if (!oldName.equals(dto.getName())) {
            changes.add(new Change("name", oldName, dto.getName()));
        }
        if (!oldDescription.equals(dto.getDescription())) {
            changes.add(new Change("description", oldDescription, dto.getDescription()));
        }

        // Логируем активность, если есть изменения
        if (!changes.isEmpty()) {
            activityLogService.logActivity(
                    user,
                    subTask.getIdTask().getIdProject(),
                    "edit",
                    "sub_task",
                    updatedSubTask.getId(),
                    changes,
                    "Подзадача \"" + updatedSubTask.getName() + "\" обновлена"
            );
        }

        return ResponseEntity.ok(updatedSubTask);
    }

    @DeleteMapping("/{subtaskId}")
    public ResponseEntity<?> deleteSubtask(@PathVariable Long subtaskId, Principal principal) {
        SubTasks subTask = subTasksRepository.findById(subtaskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Подзадача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        permissionService.checkIfProjectArchived(subTask.getIdTask().getIdProject());

        if (!permissionService.hasPermission(user.getId(), subTask.getIdTask().getIdProject().getId(), "delete_subtasks")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        activityLogService.logActivity(
                user,
                subTask.getIdTask().getIdProject(),
                "delete",
                "sub_task",
                subTask.getId(),
                null,
                "Удалена подзадача \"" + subTask.getName() + "\" из задачи \"" + subTask.getIdTask().getName() + "\""
        );
        subTasksRepository.delete(subTask);
        return ResponseEntity.ok().build();
    }

}

