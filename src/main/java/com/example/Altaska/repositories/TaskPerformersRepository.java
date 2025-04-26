package com.example.Altaska.repositories;

import com.example.Altaska.models.TaskPerformers;
import com.example.Altaska.models.Tasks;
import com.example.Altaska.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface TaskPerformersRepository extends JpaRepository<TaskPerformers, Long> {

    List<TaskPerformers> findByIdTaskId(Long taskId);

    @Transactional
    @Modifying
    @Query("DELETE FROM TaskPerformers tp WHERE tp.idTask.id = :taskId AND tp.idUser.id = :userId")
    void deleteByIdTaskIdAndIdUserId(Long taskId, Long userId);

    boolean existsByIdTaskAndIdUser(Tasks task, Users user);

    Optional<TaskPerformers> findByIdTaskAndIdUser(Tasks task, Users user);

    @Transactional
    @Modifying
    @Query("DELETE FROM TaskPerformers tp WHERE tp.idTask.id = :taskId")
    void deleteByTaskId(Long taskId);

}
