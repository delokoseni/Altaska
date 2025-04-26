package com.example.Altaska.repositories;

import com.example.Altaska.models.Attachments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentsRepository extends JpaRepository<Attachments, Long> {
    void deleteByIdTask_Id(Long taskId);
}

