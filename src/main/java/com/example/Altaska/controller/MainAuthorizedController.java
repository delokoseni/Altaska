package com.example.Altaska.controller;

import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
            String email = principal.getName();
            Optional<Users> currentUserOpt = userRepository.findByEmail(email);
            if (currentUserOpt.isPresent()) {
                Users currentUser = currentUserOpt.get();
                List<Projects> projects = projectService.getAllUserProjects(currentUser);
                model.addAttribute("projects", projects);
            }
        }
        return "mainauthorized";
    }

    @PostMapping("/create-project")
    public String createProject(@RequestParam String name,
                                @RequestParam String description,
                                @RequestParam String createdAt,
                                @RequestParam String updatedAt,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (principal != null) {
            String email = principal.getName();
            Optional<Users> currentUserOpt = userRepository.findByEmail(email);
            LocalDate clientCreatedAt = LocalDate.parse(createdAt);
            OffsetDateTime clientUpdatedAt = OffsetDateTime.parse(updatedAt);
            if (currentUserOpt.isPresent()) {
                Users user = currentUserOpt.get();
                projectService.createProject(name, description, user, clientCreatedAt, clientUpdatedAt);
                redirectAttributes.addFlashAttribute("message", "Проект создан!");
            }
        }
        return "redirect:/mainauthorized";
    }
}
