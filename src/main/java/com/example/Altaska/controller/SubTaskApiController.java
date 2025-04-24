package com.example.Altaska.controller;

import lombok.Getter;
import lombok.Setter;
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
import java.time.LocalDateTime;
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

    @PostMapping
    public List<SubTasks> createSubtasks(@PathVariable Long taskId,
                                         @RequestBody List<SubTaskDTO> subtasks,
                                         Principal principal) {
        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        permissionService.checkIfProjectArchived(task.getIdProject());

        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "edit")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа");
        }

        List<SubTasks> saved = new ArrayList<>();
        for (SubTaskDTO dto : subtasks) {
            SubTasks subTask = new SubTasks();
            subTask.setIdTask(task);
            subTask.setName(dto.getName());
            subTask.setDescription(dto.getDescription());
            subTask.setCreatedAt(LocalDate.now()); //TODO заменить на время пользователя
            subTask.setCreatedAtServer(LocalDate.now());
            saved.add(subTasksRepository.save(subTask));
        }

        return saved;
    }

    public static class SubTaskDTO {
        private String name;
        private String description;

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

}

