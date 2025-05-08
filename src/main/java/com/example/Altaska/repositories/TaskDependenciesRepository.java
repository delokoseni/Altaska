package com.example.Altaska.repositories;

import com.example.Altaska.models.TasksDependencies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskDependenciesRepository extends JpaRepository<TasksDependencies, Long> {
    void deleteByIdFromTask_Id(Long taskId);  // Удаляем по idFromTask
    void deleteByIdToTask_Id(Long taskId);

}
