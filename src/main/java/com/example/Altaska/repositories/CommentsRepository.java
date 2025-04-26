package com.example.Altaska.repositories;

import com.example.Altaska.models.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Long> {
    void deleteByIdTask_Id(Long taskId);
}
