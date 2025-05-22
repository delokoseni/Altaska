package com.example.Altaska.controller;

import com.example.Altaska.models.StatusesLog;
import com.example.Altaska.repositories.StatusesLogRepository;
import com.example.Altaska.dto.StatusLogDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statuses-log")
public class StatusesLogApiController {

    @Autowired
    private StatusesLogRepository statusesLogRepository;

    @GetMapping("/task/{taskId}")
    public List<StatusLogDto> getStatusLogsByTask(@PathVariable Long taskId) {
        return statusesLogRepository.findByIdTask_IdOrderByChangeAtDesc(taskId)
                .stream()
                .map(log -> new StatusLogDto(
                        log.getChangeAt(),
                        log.getIdUser().getEmail(),
                        log.getIdTask().getIdStatus().getName()
                ))
                .toList();
    }
}
