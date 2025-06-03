package com.example.Altaska.services;

import com.example.Altaska.models.Notifications;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.NotificationsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {

    @Autowired
    private NotificationsRepository notificationsRepository;

    public void notifyUsers(Set<Users> recipients, String type, String relatedEntityType, Long relatedEntityId, Users initiator, String content) {
        OffsetDateTime now = OffsetDateTime.now();

        for (Users user : recipients) {
            if (user.getId().equals(initiator.getId())) {
                continue;
            }

            Notifications notification = new Notifications();
            notification.setType(type);
            notification.setRelatedEntityType(relatedEntityType);
            notification.setRelatedEntityId(relatedEntityId);
            notification.setIsRead(false);
            notification.setCreatedAt(now);
            notification.setCreatedAtServer(now);
            notification.setIdUser(user);
            notification.setContent(content);
            notificationsRepository.save(notification);
        }
    }
}
