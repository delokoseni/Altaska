package com.example.Altaska.services;

import com.example.Altaska.models.ProjectMembers;
import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.ProjectMembersRepository;
import com.example.Altaska.repositories.ProjectsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectService {
    @Autowired
    private ProjectsRepository projectsRepo;

    @Autowired
    private ProjectMembersRepository membersRepo;

    public List<Projects> getAllUserProjects(Users user) {
        Set<Projects> allProjects = new HashSet<>();

        allProjects.addAll(projectsRepo.findByIdOwner(user));

        List<ProjectMembers> memberships = membersRepo.findByIdUser(user);
        for (ProjectMembers member : memberships) {
            allProjects.add(member.getIdProject());
        }

        return new ArrayList<>(allProjects);
    }

    public void createProject(String name, String description, Users owner, LocalDate createdAt,
                              OffsetDateTime updatedAt) {
        Projects project = new Projects();
        project.setName(name);
        project.setDescription(description);
        project.setIdOwner(owner);

        project.setCreatedAt(createdAt);
        project.setUpdatedAt(updatedAt);
        project.setCreatedAtServer(LocalDate.now());
        project.setUpdatedAtServer(OffsetTime.now());
        project.setUpdatedBy(owner.getEmail());

        projectsRepo.save(project);
    }

}

