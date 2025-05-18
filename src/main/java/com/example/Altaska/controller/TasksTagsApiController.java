package com.example.Altaska.controller;

import com.example.Altaska.dto.Change;
import com.example.Altaska.models.Tasks;
import com.example.Altaska.models.Tags;
import com.example.Altaska.models.TasksTags;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.TagsRepository;
import com.example.Altaska.repositories.TasksRepository;
import com.example.Altaska.repositories.TasksTagsRepository;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.services.ActivityLogService;
import com.example.Altaska.services.PermissionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/taskstags")
public class TasksTagsApiController {

    @Autowired
    private TasksTagsRepository tasksTagsRepository;

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private TagsRepository tagsRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping("/task/{taskId}")
    public List<Tags> getTagsByTask(@PathVariable Long taskId) {
        Tasks task = tasksRepository.findById(taskId).orElseThrow();
        return tasksTagsRepository.findByIdTask(task)
                .stream().map(TasksTags::getIdTag).toList();
    }

    @PostMapping("/{taskId}/add")
    public void addTagToTask(@PathVariable Long taskId, @RequestParam Long tagId, Principal principal) {
        Tasks task = tasksRepository.findById(taskId).orElseThrow();
        Tags tag = tagsRepository.findById(tagId).orElseThrow();

        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "add_task_tags")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав.");
        }
        permissionService.checkIfProjectArchived(task.getIdProject());
        TasksTags tasksTags = new TasksTags();
        tasksTags.setIdTask(task);
        tasksTags.setIdTag(tag);
        tasksTags.setAddedAtServer(LocalDateTime.now());
        tasksTagsRepository.save(tasksTags);
        activityLogService.logActivity(
                user,
                task.getIdProject(),
                "create",
                "task_tag",
                tasksTags.getId(),
                null,
                "К задаче \"" + task.getName() + "\" добавлен тег \"" + tag.getName() + "\""
        );
    }

    @DeleteMapping("/{taskId}/{tagId}")
    @Transactional
    public void removeTagFromTask(@PathVariable Long taskId,
                                  @PathVariable Long tagId,
                                  Principal principal) {
        Tasks task = tasksRepository.findById(taskId).orElseThrow();
        Tags tag = tagsRepository.findById(tagId).orElseThrow();
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (!permissionService.hasPermission(user.getId(), task.getIdProject().getId(), "remove_task_tags")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав.");
        }
        permissionService.checkIfProjectArchived(task.getIdProject());
        activityLogService.logActivity(
                user,
                task.getIdProject(),
                "delete",
                "task_tag",
                null, //Возможно тут нужен id
                null,
                "Из задачи \"" + task.getName() + "\" удалён тег \"" + tag.getName() + "\""
        );
        tasksTagsRepository.deleteByIdTaskAndIdTag(task, tag);
    }

}
