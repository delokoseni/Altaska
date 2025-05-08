package com.example.Altaska.controller;

import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Tasks;
import com.example.Altaska.models.TasksDependencies;
import com.example.Altaska.repositories.TaskDependenciesRepository;
import com.example.Altaska.repositories.TasksRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gantt")
public class GanttController {

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private TaskDependenciesRepository taskDependenciesRepository;

    @Transactional
    @PostMapping("/save")
    public ResponseEntity<?> saveGanttData(@RequestBody GanttDataDTO data) {
        for (TaskDTO dto : data.getTasks()) {
            Tasks task = tasksRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));

            // Обновление сроков и прогресса
            task.setStartTimeServer(dto.getStartTime());
            task.setDeadlineServer(dto.getDeadline());
            task.setUpdatedAtServer(LocalDateTime.now());
            tasksRepository.save(task);

            // Удаление старых зависимостей
            taskDependenciesRepository.deleteByIdFromTask_Id(task.getId());

            // Сохранение новых зависимостей
            if (dto.getDependencies() != null && !dto.getDependencies().isBlank()) {
                String[] deps = dto.getDependencies().split(",");
                for (String dep : deps) {
                    Matcher m = Pattern.compile("(\\d+)([A-Z]{2})", Pattern.CASE_INSENSITIVE).matcher(dep.trim());
                    if (m.matches()) {
                        Long toTaskId = Long.parseLong(m.group(1));
                        String type = m.group(2).toUpperCase(); // Приводим к верхнему регистру: FS, SS, FF, SF

                        Tasks toTask = tasksRepository.findById(toTaskId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task dep not found"));

                        TasksDependencies dependency = new TasksDependencies();
                        dependency.setIdFromTask(task);
                        dependency.setIdToTask(toTask);
                        dependency.setType(type);
                        taskDependenciesRepository.save(dependency);
                    } else {
                        // Можно залогировать некорректный формат, если нужно
                        System.out.println("Неверный формат зависимости: " + dep);
                    }
                }
            }
        }

        return ResponseEntity.ok("Сохранено");
    }


    @GetMapping("/project/{projectId}")
    public List<Tasks> getTasksByProject(@PathVariable Long projectId) {
        return tasksRepository.findByIdProject_Id(projectId);
    }

    @GetMapping("/project/{projectId}/dependencies")
    public List<Map<String, String>> getDependenciesByProject(@PathVariable Long projectId) {
        List<TasksDependencies> dependencies = taskDependenciesRepository
                .findByIdFromTask_IdProject_Id(projectId);

        return dependencies.stream()
                .map(dep -> Map.of(
                        "from", dep.getIdFromTask().getId().toString(),
                        "to", dep.getIdToTask().getId().toString(),
                        "type", dep.getType() // Пример: "FS", "SS"
                ))
                .collect(Collectors.toList());
    }


    @Data
    public static class GanttDataDTO {
        private List<TaskDTO> tasks;

        public List<TaskDTO> getTasks() {
            return tasks;
        }

        public void setTasks(List<TaskDTO> tasks) {
            this.tasks = tasks;
        }
    }

    @Data
    public static class TaskDTO {
        private Long id;
        private String name;
        private LocalDateTime startTime;
        private LocalDateTime deadline;
        private String dependencies; // строка вида "1FS,3SS"

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getDeadline() {
            return deadline;
        }

        public void setDeadline(LocalDateTime deadline) {
            this.deadline = deadline;
        }

        public String getDependencies() {
            return dependencies;
        }

        public void setDependencies(String dependencies) {
            this.dependencies = dependencies;
        }
    }

}
