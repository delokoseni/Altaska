package com.example.Altaska.repositories;

import com.example.Altaska.models.Tags;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TagsRepository extends JpaRepository<Tags, Long> {
    List<Tags> findByIdProjectId(Long projectId);
}
