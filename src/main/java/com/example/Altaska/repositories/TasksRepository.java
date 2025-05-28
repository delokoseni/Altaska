package com.example.Altaska.repositories;

import com.example.Altaska.models.Tasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TasksRepository extends JpaRepository<Tasks, Long> {
    List<Tasks> findByIdProject_Id(Long projectId);

    @Query("SELECT tp.idTask FROM TaskPerformers tp WHERE tp.idUser.email = :email")
    List<Tasks> findAllByPerformer(@Param("email") String email);

    @Query("SELECT tp.idTask FROM TaskPerformers tp WHERE tp.idUser.id = :userId AND tp.idTask.idProject.id = :projectId")
    List<Tasks> findTasksByUserIdAndProjectId(@Param("userId") Long userId, @Param("projectId") Long projectId);

    @Query("""
    SELECT t FROM Tasks t
    WHERE t.idProject.id = :projectId AND (
        EXISTS (
            SELECT 1 FROM TaskPerformers tp
            WHERE tp.idTask.id = t.id AND tp.idUser.id = :userId
        )
        OR NOT EXISTS (
            SELECT 1 FROM TaskPerformers tp
            WHERE tp.idTask.id = t.id
        )
    )
    """)
    List<Tasks> findTasksByUserOrNoPerformers(@Param("userId") Long userId, @Param("projectId") Long projectId);

}

