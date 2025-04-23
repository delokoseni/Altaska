package com.example.Altaska.repositories;

import com.example.Altaska.models.ProjectMembers;
import com.example.Altaska.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface ProjectMembersRepository extends JpaRepository<ProjectMembers, Long> {

    List<ProjectMembers> findByIdUser(Users user);

    List<ProjectMembers> findByIdProjectId(Long projectId);

    Optional<ProjectMembers> findByIdProjectIdAndIdUserId(Long projectId, Long userId);

    Optional<ProjectMembers> findByConfirmationToken(String confirmationToken);
}
