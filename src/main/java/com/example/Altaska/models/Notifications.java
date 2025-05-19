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

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private Users idUser;

    @Column(name = "created_at_server", nullable = false)
    private OffsetDateTime createdAtServer;

    @Column(name = "content", nullable = false)
    private String content;

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public boolean getIsRead() {
        return isRead;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Users getIdUser() {
        return idUser;
    }

    public OffsetDateTime getCreatedAtServer() {
        return createdAtServer;
    }

    public  String  getContent() { return content; }

    public void setType(String type) {
        this.type = type;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setIdUser(Users idUser) {
        this.idUser = idUser;
    }

    public void setCreatedAtServer(OffsetDateTime createdAtServer) {
        this.createdAtServer = createdAtServer;
    }

    public void setContent(String content) { this.content = content; }
}
