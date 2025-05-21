package com.example.Altaska.controller;

import com.example.Altaska.models.ActivityLog;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.ActivityLogRepository;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class ActivityLogApiController {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getProjectLogs(@PathVariable Long projectId, Principal principal) {
        Users user = usersRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!permissionService.hasPermission(user.getId(), projectId, "view_project_log")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        List<ActivityLog> logs = activityLogRepository.findByIdProjectIdOrderByActivityDateDesc(projectId);
        return ResponseEntity.ok(logs);
    }
}
