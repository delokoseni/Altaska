package com.example.Altaska.repositories;

import com.example.Altaska.models.StatusesLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusesLogRepository extends JpaRepository<StatusesLog, Long> {
    void deleteByIdTask_Id(Long taskId);
    List<StatusesLog> findByIdTask_IdOrderByChangeAtDesc(Long taskId);
}
