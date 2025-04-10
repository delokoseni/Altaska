package com.example.Altaska.controller;

import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class MainAuthorizedController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UsersRepository userRepository;

    @GetMapping("/mainauthorized")
    public String ShowLogin(Model model, Principal principal) {
        if (principal != null) {
            String email = principal.getName(); // имя текущего пользователя (у тебя это email)
            Optional<Users> currentUserOpt = userRepository.findByEmail(email);
            if (currentUserOpt.isPresent()) {
                Users currentUser = currentUserOpt.get();
                List<Projects> projects = projectService.getAllUserProjects(currentUser);
                model.addAttribute("projects", projects);
            }
        }
        return "mainauthorized";
    }

}
