package com.example.Altaska.repositories;

import com.example.Altaska.models.StatusesLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusesLogRepository extends JpaRepository<StatusesLog, Long> {
    // Метод для удаления записей по taskId
    void deleteByIdTask_Id(Long taskId);
}
