package com.example.Altaska.services;

import com.example.Altaska.models.ProjectMembers;
import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Roles;
import com.example.Altaska.repositories.ProjectMembersRepository;
import com.example.Altaska.repositories.ProjectsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final ProjectMembersRepository projectMembersRepository;

    private final ProjectsRepository projectsRepository;

    public boolean hasPermission(Long userId, Long projectId, String action) {
        Optional<Projects> project = projectsRepository.findById(projectId);
        if (project.isPresent()) {
            Projects p = project.get();
            if (Objects.equals(p.getIdOwner().getId(), userId)) {
                return true;
            }
        }
        Optional<ProjectMembers> memberOpt = projectMembersRepository.findByIdProjectIdAndIdUserId(projectId, userId);
        if (memberOpt.isEmpty()) {
            return false;
        }
        ProjectMembers member = memberOpt.get();
        if (!member.isConfirmed()) {
            return false;
        }
        Roles role = member.getIdRole();
        if (role == null) {
            return false;
        }
        JsonNode permissions = role.getPermissions();
        if (permissions == null || !permissions.has(action)) {
            return false;
        }
        return permissions.get(action).asBoolean(false);
    }

    public boolean checkIfProjectArchived(Projects project) {
        return project.isArchived();
    }

}

