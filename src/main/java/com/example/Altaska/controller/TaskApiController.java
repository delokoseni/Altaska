package com.example.Altaska.controller;

import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Statuses;
import com.example.Altaska.models.Tasks;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.ProjectsRepository;
import com.example.Altaska.repositories.StatusesRepository;
import com.example.Altaska.repositories.TasksRepository;
import com.example.Altaska.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

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

    @GetMapping("/project/{projectId}")
    public List<Tasks> getTasksByProject(@PathVariable Long projectId) {
        return tasksRepository.findByIdProject_Id(projectId);
    }

    @PostMapping("/create/{projectId}")
    public Tasks createTask(@PathVariable Long projectId,
                            @RequestParam String name,
                            @RequestParam String description,
                            Principal principal) {
        Optional<Projects> projectOpt = projectsRepository.findById(projectId);
        Optional<Users> userOpt = usersRepository.findByEmail(principal.getName());

        if (projectOpt.isPresent() && userOpt.isPresent()) {
            Tasks task = new Tasks();
            task.SetName(name);
            task.SetDescription(description);
            task.SetIdProject(projectOpt.get());
            task.SetIdCreator(userOpt.get());

            LocalDate nowDate = LocalDate.now();
            LocalDateTime nowDateTime = LocalDateTime.now();
            OffsetDateTime nowOffset = OffsetDateTime.now();

            task.SetCreatedAt(nowDate); //todo Переделать
            task.SetUpdatedAt(nowOffset); //todo Переделать
            task.SetCreatedAtServer(nowDate);
            task.SetUpdatedAtServer(nowDateTime);
            task.SetStatusChangeAtServer(nowDateTime);
            task.SetUpdatedBy(principal.getName());
            task.SetTimeSpent(0L);
            Statuses status = statusesRepository.findById(1L).orElseThrow(() -> new RuntimeException("Статус не найден"));
            task.SetIdStatus(status);
            return tasksRepository.save(task);
        } else {
            throw new RuntimeException("Проект или пользователь не найден");
        }
    }

}

