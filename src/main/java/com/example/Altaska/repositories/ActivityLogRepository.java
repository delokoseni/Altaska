package com.example.Altaska.repositories;

import java.util.List;
import com.example.Altaska.models.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByIdProjectIdOrderByActivityDateDesc(Long projectId);
}
