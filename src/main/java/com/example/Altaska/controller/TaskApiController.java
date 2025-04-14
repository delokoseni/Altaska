package com.example.Altaska.controller;

import com.example.Altaska.models.*;
import com.example.Altaska.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
            return tasksRepository.save(task);
        } else {
            throw new RuntimeException("Проект или пользователь не найден");
        }
    }
}

