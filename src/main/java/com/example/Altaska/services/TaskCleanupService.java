package com.example.Altaska.services;

import com.example.Altaska.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskCleanupService {

    private final CommentsRepository commentsRepository;

    private final SubTasksRepository subtasksRepository;

    private final TasksTagsRepository tasksTagsRepository;

    private final TaskPerformersRepository taskPerformersRepository;

    private final StatusesLogRepository statusesLogRepository;

    private final AttachmentsRepository attachmentsRepository;

    private final TaskDependenciesRepository taskDependenciesRepository;

    @Transactional
    public void deleteTaskDependencies(Long taskId) {
        taskPerformersRepository.deleteByIdTask_Id(taskId);
        tasksTagsRepository.deleteByTaskId(taskId);
        statusesLogRepository.deleteByIdTask_Id(taskId);
        attachmentsRepository.deleteByIdTask_Id(taskId);
        taskDependenciesRepository.deleteByIdFromTask_Id(taskId);
        taskDependenciesRepository.deleteByIdToTask_Id(taskId);
    }

    @Transactional
    public void deleteComments(Long taskId) {
        commentsRepository.deleteByIdTask_Id(taskId);
    }

    @Transactional
    public void deleteSubtasks(Long taskId) {
        subtasksRepository.deleteByIdTask_Id(taskId);
    }
}
