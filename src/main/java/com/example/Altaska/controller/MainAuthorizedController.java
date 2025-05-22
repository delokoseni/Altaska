package com.example.Altaska.controller;

import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.services.ActivityLogService;
import com.example.Altaska.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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

    @Autowired
    private ActivityLogService activityLogService;


    @GetMapping("/mainauthorized")
    public String ShowLogin(Model model, Principal principal) {
        if (principal != null) {
            String email = principal.getName();
            Optional<Users> currentUserOpt = userRepository.findByEmail(email);
            if (currentUserOpt.isPresent()) {
                Users currentUser = currentUserOpt.get();
                List<Projects> projects = projectService.getAllUserProjects(currentUser);
                model.addAttribute("projects", projects);
                model.addAttribute("SYNCFUSION_LICENSE", System.getProperty("SYNCFUSION_LICENSE"));

            }
        }
        return "mainauthorized";
    }

    @PostMapping("/create-project")
    public ResponseEntity<?> createProject(@RequestParam String name,
                                @RequestParam String description,
                                @RequestParam String createdAt,
                                @RequestParam String updatedAt,
                                Principal principal) {
        if (principal != null) {
            String email = principal.getName();
            Optional<Users> currentUserOpt = userRepository.findByEmail(email);
            LocalDate clientCreatedAt = LocalDate.parse(createdAt);
            OffsetDateTime clientUpdatedAt = OffsetDateTime.parse(updatedAt);
            if (currentUserOpt.isPresent()) {
                Users user = currentUserOpt.get();
                if (name != null && name.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("Имя проекта не может быть пустым!");
                }
                Projects newProject = projectService.createProject(name, description, user, clientCreatedAt, clientUpdatedAt);
                activityLogService.logActivity(
                        user,
                        newProject,
                        "create",
                        "project",
                        newProject.getId(),
                        null,
                        "Создан проект с названием: " + name
                );
            }
        }
        return ResponseEntity.ok("Проект создан!");
    }

    @GetMapping("/current-user-email")
    @ResponseBody
    public String getCurrentUserEmail(Principal principal) {
        if (principal != null) {
            return principal.getName();  
        }
        return "";
    }
}
