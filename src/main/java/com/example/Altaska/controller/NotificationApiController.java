package com.example.Altaska.controller;

import com.example.Altaska.dto.NotificationDto;
import com.example.Altaska.models.Notifications;
import com.example.Altaska.models.Tasks;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.NotificationsRepository;
import com.example.Altaska.repositories.TasksRepository;
import com.example.Altaska.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationApiController {

    @Autowired
    private NotificationsRepository notificationsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TasksRepository tasksRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(Principal principal) {
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        List<Notifications> notifications = notificationsRepository.findAllByIdUserOrderByCreatedAtDesc(user);

        List<NotificationDto> dtos = notifications.stream().map(n -> {
            String relatedName = "";
            if ("tasks".equalsIgnoreCase(n.getRelatedEntityType())) {
                Tasks task = tasksRepository.findById(n.getRelatedEntityId()).orElse(null);
                if (task != null) relatedName = task.getName();
            }

            return new NotificationDto(
                    n.getId(),
                    n.getType(),
                    n.getCreatedAtServer().format(formatter),
                    n.getIsRead(),
                    n.getRelatedEntityType(),
                    n.getRelatedEntityId(),
                    relatedName,
                    n.getContent()
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
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

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearNotifications(Principal principal) {
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Notifications> userNotifications = notificationsRepository.findAllByIdUser(user);
        notificationsRepository.deleteAll(userNotifications);

        return ResponseEntity.ok().build();
    }

}
