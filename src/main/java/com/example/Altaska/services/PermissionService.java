package com.example.Altaska.services;

import com.example.Altaska.models.Projects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PermissionService {

    public boolean hasPermission(Long userId, Long projectId, String action) {
        // TODO: Сделать реальную проверку по БД
        return true;
    }

    public void checkIfProjectArchived(Projects project) {
        if (project.getIsArchived()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Проект архивирован и не может быть изменён");
        }
    }

}

