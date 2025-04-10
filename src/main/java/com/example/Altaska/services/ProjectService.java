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
            allProjects.add(member.GetIdProject());
        }

        return new ArrayList<>(allProjects);
    }

    public void createProject(String name, String description, Users owner) {
        Projects project = new Projects();
        project.SetName(name);
        project.SetDescription(description);
        project.SetIdOwner(owner);

        project.SetCreatedAt(LocalDate.now());  //todo Заменить на время клиента
        project.SetUpdatedAt(OffsetDateTime.now());  //todo Заменить на время клиента
        project.SetCreatedAtServer(LocalDate.now());
        project.SetUpdatedAtServer(OffsetTime.now());
        project.SetUpdatedBy(owner.GetEmail());

        projectsRepo.save(project);
    }

}

