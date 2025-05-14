package com.example.Altaska.controller;

import com.example.Altaska.models.*;
import com.example.Altaska.repositories.*;
import com.example.Altaska.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/task-performers")
public class TaskPerformerApiController {

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TaskPerformersRepository taskPerformersRepository;

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskPerformers(@PathVariable Long taskId) {
        List<TaskPerformers> performers = taskPerformersRepository.findByIdTaskId(taskId);

        List<Map<String, Object>> result = performers.stream().map(tp -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", tp.getIdUser().getId());
            map.put("email", tp.getIdUser().getEmail());
            map.put("addedAt", tp.getAddedAtServer());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{taskId}")
    public ResponseEntity<?> addPerformer(@PathVariable Long taskId,
                                          @RequestParam Long userId,
                                          Principal principal) {
        Tasks task = tasksRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Задача не найдена"));
        permissionService.checkIfProjectArchived(task.getIdProject());

        Users performer = usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Users currentUser = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Текущий пользователь не найден"));

        if (!permissionService.hasPermission(currentUser.getId(), task.getIdProject().getId(), "add_task_performers")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав.");
        }

        if (taskPerformersRepository.existsByIdTaskAndIdUser(task, performer)) {
            return ResponseEntity.badRequest().body("Пользователь уже является исполнителем этой задачи");
        }

        TaskPerformers newPerformer = new TaskPerformers();
        newPerformer.setIdTask(task);
        newPerformer.setIdUser(performer);
        newPerformer.setAddedAtServer(OffsetDateTime.now());

        taskPerformersRepository.save(newPerformer);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> removePerformer(@PathVariable Long taskId,
                                             @RequestParam Long userId,
                                             Principal principal) {
        Tasks task = tasksRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Задача не найдена"));
        permissionService.checkIfProjectArchived(task.getIdProject());

        Users currentUser = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Текущий пользователь не найден"));

        if (!permissionService.hasPermission(currentUser.getId(), task.getIdProject().getId(), "remove_task_performers")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав.");
        }

        taskPerformersRepository.deleteByIdTaskIdAndIdUserId(taskId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/assign")
    public ResponseEntity<?> assignPerformer(@PathVariable Long taskId, Principal principal) {
        Tasks task = tasksRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Задача не найдена"));
        permissionService.checkIfProjectArchived(task.getIdProject());

        Users currentUser = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Текущий пользователь не найден"));

        if (!permissionService.hasPermission(currentUser.getId(), task.getIdProject().getId(), "accept_tasks")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав.");
        }

        if (taskPerformersRepository.existsByIdTaskAndIdUser(task, currentUser)) {
            return ResponseEntity.badRequest().body("Вы уже являетесь исполнителем этой задачи");
        }

        TaskPerformers newPerformer = new TaskPerformers();
        newPerformer.setIdTask(task);
        newPerformer.setIdUser(currentUser);
        newPerformer.setAddedAtServer(OffsetDateTime.now());

        taskPerformersRepository.save(newPerformer);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{taskId}/unassign")
    public ResponseEntity<?> unassignPerformer(@PathVariable Long taskId, Principal principal) {
        Tasks task = tasksRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Задача не найдена"));
        permissionService.checkIfProjectArchived(task.getIdProject());

        Users currentUser = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Текущий пользователь не найден"));

        if (!permissionService.hasPermission(currentUser.getId(), task.getIdProject().getId(), "reject_tasks")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав.");
        }

        TaskPerformers performer = taskPerformersRepository.findByIdTaskAndIdUser(task, currentUser)
                .orElseThrow(() -> new RuntimeException("Вы не являетесь исполнителем этой задачи"));

        taskPerformersRepository.delete(performer);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{taskId}/is-assigned")
    public ResponseEntity<?> isAssigned(@PathVariable Long taskId, Principal principal) {
        Tasks task = tasksRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Задача не найдена"));
        Users currentUser = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Текущий пользователь не найден"));
        boolean isAssigned = taskPerformersRepository.existsByIdTaskAndIdUser(task, currentUser);
        return ResponseEntity.ok(isAssigned);
    }
}
