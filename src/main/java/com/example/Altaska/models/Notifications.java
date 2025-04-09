package com.example.Altaska.models;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notifications")
public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @Column(name = "related_entity_type", length = 50, nullable = false)
    private String relatedEntityType;

    @Column(name = "related_entity_id", nullable = false)
    private Long relatedEntityId;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "id_user", nullable = false)
    private Integer idUser; //todo Переделать

    @Column(name = "created_at_server", nullable = false)
    private OffsetDateTime createdAtServer;

    public Long GetId() {
        return id;
    }

    public String GetType() {
        return type;
    }

    public String GetRelatedEntityType() {
        return relatedEntityType;
    }

    public Long GetRelatedEntityId() {
        return relatedEntityId;
    }

    public boolean GetIsRead() {
        return isRead;
    }

    public OffsetDateTime GetCreatedAt() {
        return createdAt;
    }

    public Integer GetIdUser() {
        return idUser; //todo Возможно переделать
    }

    public OffsetDateTime GetCreatedAtServer() {
        return createdAtServer;
    }

    public void SetType(String type) {
        this.type = type;
    }

    public void SetRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public void SetRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public void SetIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public void SetCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void SetIdUser(Integer idUser) {
        this.idUser = idUser; //todo Возможно переделать
    }

    public void SetCreatedAtServer(OffsetDateTime createdAtServer) {
        this.createdAtServer = createdAtServer;
    }
}
