package com.example.Altaska.repositories;

import com.example.Altaska.models.TasksTags;
import com.example.Altaska.models.Tasks;
import com.example.Altaska.models.Tags;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TasksTagsRepository extends JpaRepository<TasksTags, Long> {

    List<TasksTags> findByIdTask(Tasks task);

    void deleteByIdTaskAndIdTag(Tasks task, Tags tag);

    @Transactional
    @Modifying
    @Query("DELETE FROM TasksTags tt WHERE tt.idTask.id = :taskId")
    void deleteByTaskId(Long taskId);
}
