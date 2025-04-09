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

    @Column(name = "id_task", nullable = false)
    private Integer idTask; //todo Переделать

    @Column(name = "id_user", nullable = false)
    private Integer idUser; //todo Переделать

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

    public Integer GetIdTask() {
        return idTask; //todo Возможно переделать
    }

    public Integer GetIdUser() {
        return idUser; //todo Возможно переделать
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

    public void SetIdTask(Integer idTask) {
        this.idTask = idTask; //todo Возможно переделать
    }

    public void SetIdUser(Integer idUser) {
        this.idUser = idUser; //todo Возможно переделать
    }

    public void SetCreatedAtServer(OffsetDateTime createdAtServer) {
        this.createdAtServer = createdAtServer;
    }
}
