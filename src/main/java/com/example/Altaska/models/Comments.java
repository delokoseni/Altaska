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

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "id_task", referencedColumnName = "id")
    private Tasks idTask;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private Users idUser;

    @Column(name = "created_at_server", nullable = false)
    private OffsetDateTime createdAtServer;

    public Long GetId() {
        return id;
    }

    public String GetContent() {
        return content;
    }

    public OffsetDateTime GetCreatedAt() {
        return createdAt;
    }

    public Tasks GetIdTask() {
        return idTask;
    }

    public Users GetIdUser() {
        return idUser;
    }

    public OffsetDateTime GetCreatedAtServer() {
        return createdAtServer;
    }

    public void SetContent(String content) {
        this.content = content;
    }

    public void SetCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void SetIdTask(Tasks idTask) {
        this.idTask = idTask;
    }

    public void SetIdUser(Users idUser) {
        this.idUser = idUser;
    }

    public void SetCreatedAtServer(OffsetDateTime createdAtServer) {
        this.createdAtServer = createdAtServer;
    }
}
