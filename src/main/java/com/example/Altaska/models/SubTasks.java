package com.example.Altaska.models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subtasks")
public class SubTasks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_task", nullable = false)
    private Integer idTask; //todo Переделать

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @Column(name = "created_at_server", nullable = false)
    private LocalDate createdAtServer;

    @Column(name = "completed_at_server", nullable = false)
    private LocalDateTime completedAtServer;

    @Column(name = "id_user", nullable = false)
    private Integer idUser; //todo Переделать

    public Long GetId() {
        return id;
    }

    public Integer GetIdTask() {
        return idTask; //todo Возможно переделать
    }

    public String GetName() {
        return name;
    }

    public String GetDescription() {
        return description;
    }

    public LocalDate GetCreatedAt() {
        return createdAt;
    }

    public boolean GetIsCompleted() {
        return isCompleted;
    }

    public LocalDate GetCreatedAtServer() {
        return createdAtServer;
    }

    public LocalDateTime GetCompletedAtServer() {
        return completedAtServer;
    }

    public Integer GetIdUser() {
        return idUser; //todo Возможно переделать
    }

    public void SetIdTask(Integer idTask) {
        this.idTask = idTask; //todo Возможно переделать
    }

    public void SetName(String name) {
        this.name = name;
    }

    public void SetDescription(String description) {
        this.description = description;
    }

    public void SetCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public void SetIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public void SetCreatedAtServer(LocalDate createdAtServer) {
        this.createdAtServer = createdAtServer;
    }

    public void SetCompletedAtServer(LocalDateTime completedAtServer) {
        this.completedAtServer = completedAtServer;
    }

    public void SetIdUser(Integer idUser) {
        this.idUser = idUser; //todo Возможно переделать
    }
}
