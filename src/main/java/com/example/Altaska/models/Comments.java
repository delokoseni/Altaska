package com.example.Altaska.models;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "comments")
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "id_task", referencedColumnName = "id")
    private Tasks idTask;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private Users idUser;

    @Column(name = "created_at_server", nullable = false, updatable = false)
    private OffsetDateTime createdAtServer;

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Tasks getIdTask() {
        return idTask;
    }

    public Users getIdUser() {
        return idUser;
    }

    public OffsetDateTime getCreatedAtServer() {
        return createdAtServer;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setIdTask(Tasks idTask) {
        this.idTask = idTask;
    }

    public void setIdUser(Users idUser) {
        this.idUser = idUser;
    }

    public void setCreatedAtServer(OffsetDateTime createdAtServer) {
        this.createdAtServer = createdAtServer;
    }
}
