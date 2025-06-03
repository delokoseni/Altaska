package com.example.Altaska.repositories;

import com.example.Altaska.models.ProjectMembers;
import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Roles;
import com.example.Altaska.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface ProjectMembersRepository extends JpaRepository<ProjectMembers, Long> {
    List<ProjectMembers> findByIdProjectId(Long projectId);
    Optional<ProjectMembers> findByIdProjectIdAndIdUserId(Long projectId, Long userId);
    Optional<ProjectMembers> findByConfirmationToken(String confirmationToken);
    boolean existsByIdProjectAndInviteeEmail(Projects project, String inviteeEmail);
    List<ProjectMembers> findByIdUserAndConfirmedTrue(Users user);
    List<ProjectMembers> findByIdProjectIdAndConfirmedTrue(Long projectId);
    boolean existsByIdRole(Roles role);
}
