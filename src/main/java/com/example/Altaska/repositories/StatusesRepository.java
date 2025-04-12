package com.example.Altaska.repositories;

import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Statuses;
import com.example.Altaska.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusesRepository extends JpaRepository<Statuses, Long> {
    Optional<Statuses> findById(long id);
}
