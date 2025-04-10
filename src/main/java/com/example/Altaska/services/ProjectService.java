package com.example.Altaska.services;

import com.example.Altaska.models.ProjectMembers;
import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.ProjectMembersRepository;
import com.example.Altaska.repositories.ProjectsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}

