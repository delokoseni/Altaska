package com.example.Altaska.repositories;

import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolesRepository extends JpaRepository<Roles, Long> {
    List<Roles> findByIdProject_IdOrIdProjectIsNull(Long projectId);
    boolean existsByNameAndIdProject(String name, Projects project);
}
