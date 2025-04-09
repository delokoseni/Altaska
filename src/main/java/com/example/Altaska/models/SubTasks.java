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

    @ManyToOne
    @JoinColumn(name = "id_task", referencedColumnName = "id")
    private Tasks idTask;

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

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private Users idUser;

    public Long GetId() {
        return id;
    }

    public Tasks GetIdTask() {
        return idTask;
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

    public Users GetIdUser() {
        return idUser;
    }

    public void SetIdTask(Tasks idTask) {
        this.idTask = idTask;
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

    public void SetIdUser(Users idUser) {
        this.idUser = idUser;
    }
}
