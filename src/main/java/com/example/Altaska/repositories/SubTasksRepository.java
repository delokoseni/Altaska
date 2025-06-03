package com.example.Altaska.repositories;

import com.example.Altaska.models.SubTasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubTasksRepository extends JpaRepository<SubTasks, Long> {
    List<SubTasks> findByIdTask_Id(Long taskId);
    void deleteByIdTask_Id(Long taskId);
}
