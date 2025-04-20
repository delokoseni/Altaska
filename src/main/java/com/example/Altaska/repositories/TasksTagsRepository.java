package com.example.Altaska.repositories;

import com.example.Altaska.models.TasksTags;
import com.example.Altaska.models.Tasks;
import com.example.Altaska.models.Tags;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TasksTagsRepository extends JpaRepository<TasksTags, Long> {
    List<TasksTags> findByIdTask(Tasks task);
    void deleteByIdTaskAndIdTag(Tasks task, Tags tag);
}
