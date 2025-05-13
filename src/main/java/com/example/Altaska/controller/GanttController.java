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
import java.util.HashMap;
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

            // Удаление старых входящих зависимостей
            taskDependenciesRepository.deleteByIdToTask_Id(task.getId());

            if (dto.getDependencies() != null && !dto.getDependencies().isBlank()) {
                String[] deps = dto.getDependencies().split(",");

                Pattern pattern = Pattern.compile("(\\d+)([FS]{2})([+-](\\d+(\\.\\d+)?))?\\s*(d(ays)?)?", Pattern.CASE_INSENSITIVE);

                for (String dep : deps) {
                    dep = dep.trim();
                    Matcher matcher = pattern.matcher(dep);

                    if (matcher.matches()) {
                        Long fromTaskId = Long.parseLong(matcher.group(1));
                        String type = matcher.group(2).toUpperCase();

                        Double lag = null;
                        if (matcher.group(3) != null) {
                            lag = Double.parseDouble(matcher.group(3)); // с плюсом или минусом
                        }

                        Tasks fromTask = tasksRepository.findById(fromTaskId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task dep not found"));

                        TasksDependencies dependency = new TasksDependencies();
                        dependency.setIdFromTask(fromTask);
                        dependency.setIdToTask(task);
                        dependency.setType(type);
                        dependency.setLag(lag);

                        taskDependenciesRepository.save(dependency);
                    } else {
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
    public List<Map<String, Object>> getDependenciesByProject(@PathVariable Long projectId) {
        List<TasksDependencies> dependencies = taskDependenciesRepository.findByIdToTask_IdProject_Id(projectId); // toTask — зависимая задача

        return dependencies.stream()
                .map(dep -> {
                    Map<String, Object> map = new HashMap<>();

                    String from = dep.getIdFromTask().getId().toString(); // та, от которой зависит (предшественник)
                    String to = dep.getIdToTask().getId().toString();     // та, которая зависит (последователь)
                    String type = dep.getType();

                    Double lag = dep.getLag();
                    String lagStr = "";
                    if (lag != null && lag != 0.0) {
                        lagStr = (lag > 0 ? "+" : "") + lag + "d";
                    }

                    map.put("from", from);
                    map.put("to", to);
                    map.put("type", type + lagStr); // например: FS+2d

                    return map;
                })
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
