package com.example.Altaska.controllers.api;

import com.example.Altaska.models.Tasks;
import com.example.Altaska.models.Tags;
import com.example.Altaska.models.TasksTags;
import com.example.Altaska.repositories.TagsRepository;
import com.example.Altaska.repositories.TasksRepository;
import com.example.Altaska.repositories.TasksTagsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/task/{taskId}")
    public List<Tags> getTagsByTask(@PathVariable Long taskId) {
        Tasks task = tasksRepository.findById(taskId).orElseThrow();
        return tasksTagsRepository.findByIdTask(task)
                .stream().map(TasksTags::getIdTag).toList();
    }

    @PostMapping("/{taskId}/add")
    public void addTagToTask(@PathVariable Long taskId, @RequestParam Long tagId) {
        Tasks task = tasksRepository.findById(taskId).orElseThrow();
        Tags tag = tagsRepository.findById(tagId).orElseThrow();

        TasksTags tasksTags = new TasksTags();
        tasksTags.setIdTask(task);
        tasksTags.setIdTag(tag);
        tasksTags.setAddedAtServer(LocalDateTime.now());
        tasksTagsRepository.save(tasksTags);
    }

    @DeleteMapping("/{taskId}/{tagId}")
    @Transactional
    public void removeTagFromTask(@PathVariable Long taskId, @PathVariable Long tagId) {
        Tasks task = tasksRepository.findById(taskId).orElseThrow();
        Tags tag = tagsRepository.findById(tagId).orElseThrow();
        tasksTagsRepository.deleteByIdTaskAndIdTag(task, tag);
    }
}
