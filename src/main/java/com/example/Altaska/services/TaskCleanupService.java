package com.example.Altaska.services;

import com.example.Altaska.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskCleanupService {

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private SubTasksRepository subtasksRepository;

    @Autowired
    private TasksTagsRepository tasksTagsRepository;

    @Autowired
    private TaskPerformersRepository taskPerformersRepository;

    @Autowired
    private StatusesLogRepository statusesLogRepository;

    @Autowired
    private AttachmentsRepository attachmentsRepository;

    @Autowired
    private TaskDependenciesRepository taskDependenciesRepository;

    @Transactional
    public void deleteTaskDependencies(Long taskId) {
        taskPerformersRepository.deleteByTaskId(taskId);
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
