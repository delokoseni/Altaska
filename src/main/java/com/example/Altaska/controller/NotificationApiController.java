package com.example.Altaska.controller;

import com.example.Altaska.models.Notifications;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.NotificationsRepository;
import com.example.Altaska.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationApiController {

    @Autowired
    private NotificationsRepository notificationsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping
    public ResponseEntity<List<Notifications>> getNotifications(Principal principal) {
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        List<Notifications> notifications = notificationsRepository.findAllByIdUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/read/{notificationId}")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId, Principal principal) {
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Notifications notification = notificationsRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));

        if (!notification.getIdUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Недостаточно прав");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notificationsRepository.save(notification);
        }
        return ResponseEntity.ok().build();
    }
}
