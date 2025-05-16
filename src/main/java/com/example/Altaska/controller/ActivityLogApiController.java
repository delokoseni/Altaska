package com.example.Altaska.controller;

import com.example.Altaska.models.ActivityLog;
import com.example.Altaska.repositories.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class ActivityLogApiController {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @GetMapping("/project/{projectId}")
    public List<ActivityLog> getProjectLogs(@PathVariable Long projectId) {
        return activityLogRepository.findByIdProjectIdOrderByActivityDateDesc(projectId);
    }
}
