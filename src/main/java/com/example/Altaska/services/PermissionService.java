package com.example.Altaska.services;

import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    public boolean hasPermission(Long userId, Long projectId, String action) {
        // TODO: Сделать реальную проверку по БД
        return true;
    }
}

