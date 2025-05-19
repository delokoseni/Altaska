package com.example.Altaska.dto;

public class NotificationDto {
    private Long id;
    private String type;
    private String createdAt;
    private Boolean isRead;
    private String relatedEntityType;
    private Long relatedEntityId;
    private String relatedEntityName;
    private String content;

    public NotificationDto(Long id, String type, String createdAt, Boolean isRead,
                           String relatedEntityType, Long relatedEntityId, String relatedEntityName, String content) {
        this.id = id;
        this.type = type;
        this.createdAt = createdAt;
        this.isRead = isRead;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.relatedEntityName = relatedEntityName;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public  String getContent() {
        return content;
    }

    public void setContent() {
        this.content = content;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getRelatedEntityName() {
        return relatedEntityName;
    }

    public void setRelatedEntityName(String relatedEntityName) {
        this.relatedEntityName = relatedEntityName;
    }
}