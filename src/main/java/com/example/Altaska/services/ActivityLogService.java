package com.example.Altaska.services;

import com.example.Altaska.dto.Change;
import com.example.Altaska.models.ActivityLog;
import com.example.Altaska.models.Projects;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.ActivityLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    private final ObjectMapper objectMapper;

    public void logActivity(
            Users user,
            Projects project,
            String action,
            String entityType,
            Long entityId,
            List<Change> changes,
            String message
    ) {
        ObjectNode details = objectMapper.createObjectNode();

        details.put("action", action);

        ObjectNode entityNode = objectMapper.createObjectNode();
        entityNode.put("type", entityType);
        entityNode.put("id", entityId);
        details.set("entity", entityNode);

        if (changes != null && !changes.isEmpty()) {
            ArrayNode changesArray = objectMapper.createArrayNode();
            for (Change change : changes) {
                ObjectNode changeNode = objectMapper.createObjectNode();
                changeNode.put("field", change.getField());
                changeNode.putPOJO("old", change.getOldValue());
                changeNode.putPOJO("new", change.getNewValue());
                changesArray.add(changeNode);
            }
            details.set("changes", changesArray);
        }

        details.put("message", message);

        ActivityLog log = new ActivityLog();
        log.setDetails(details);
        log.setActivityDate(OffsetDateTime.now());
        log.setActivityDateServer(OffsetDateTime.now());
        log.setIdUser(user);
        log.setIdProject(project);

        activityLogRepository.save(log);
    }
}
