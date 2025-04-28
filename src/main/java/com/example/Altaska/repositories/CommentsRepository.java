package com.example.Altaska.repositories;

import com.example.Altaska.models.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Long> {
    void deleteByIdTask_Id(Long taskId);
    List<Comments> findByIdTask_Id(Long taskId);
}
