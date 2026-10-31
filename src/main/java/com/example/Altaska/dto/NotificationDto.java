package com.example.Altaska.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String type;
    private String createdAt;
    private Boolean isRead;
    private String relatedEntityType;
    private Long relatedEntityId;
    private String relatedEntityName;
    private String content;
}