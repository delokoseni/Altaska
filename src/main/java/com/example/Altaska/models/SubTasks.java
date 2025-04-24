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

    @Column(name = "completed_at_server")
    private LocalDateTime completedAtServer;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private Users idUser;

    public Long getId() {
        return id;
    }

    public Tasks getIdTask() {
        return idTask;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public boolean getIsCompleted() {
        return isCompleted;
    }

    public LocalDate getCreatedAtServer() {
        return createdAtServer;
    }

    public LocalDateTime getCompletedAtServer() {
        return completedAtServer;
    }

    public Users getIdUser() {
        return idUser;
    }

    public void setIdTask(Tasks idTask) {
        this.idTask = idTask;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public void setCreatedAtServer(LocalDate createdAtServer) {
        this.createdAtServer = createdAtServer;
    }

    public void setCompletedAtServer(LocalDateTime completedAtServer) {
        this.completedAtServer = completedAtServer;
    }

    public void setIdUser(Users idUser) {
        this.idUser = idUser;
    }
}
