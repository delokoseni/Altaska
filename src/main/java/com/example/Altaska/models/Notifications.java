package com.example.Altaska.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Setter
@Getter
@Entity
@Table(name = "notifications")
public class Notifications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
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

    public boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

}
