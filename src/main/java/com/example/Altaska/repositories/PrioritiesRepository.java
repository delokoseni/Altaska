package com.example.Altaska.repositories;

import com.example.Altaska.models.Priorities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrioritiesRepository extends JpaRepository<Priorities, Long> {
    List<Priorities> findByLevel(Long level);
    Optional<Priorities> findByName(String name);
}
