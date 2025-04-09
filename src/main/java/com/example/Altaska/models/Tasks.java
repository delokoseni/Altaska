package com.example.Altaska.models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Tasks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "id_status", nullable = false)
    private Integer idStatus; //todo Переделать

    @Column(name = "id_priority")
    private Integer idPriority; //todo Переделать

    @Column(name = "id_creator", nullable = false)
    private Integer idCreator; //todo Переделать

    @Column(name = "time_spent")
    private Duration timeSpent;

    @Column(name = "id_project", nullable = false)
    private Integer idProject; //todo Переделать

    @Column(name = "created_at_server", nullable = false)
    private LocalDate createdAtServer;

    @Column(name = "updated_at_server", nullable = false)
    private LocalDateTime updatedAtServer;

    @Column(name = "deadline_server")
    private LocalDateTime deadlineServer;

    @Column(name = "updated_by", length = 254, nullable = false)
    private String updatedBy;

    @Column(name = "status_change_at_server", nullable = false)
    private LocalDateTime statusChangeAtServer;

    public Long GetId() {
        return id;
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

    public OffsetDateTime GetUpdatedAt() {
        return updatedAt;
    }

    public Integer GetIdStatus() {
        return idStatus; //todo Возможность переделать
    }

    public Integer GetIdPriority() {
        return idPriority; //todo Возможность переделать
    }

    public Integer GetIdCreator() {
        return idCreator; //todo Возможность переделать
    }

    public Duration GetTimeSpent() {
        return timeSpent;
    }

    public Integer GetIdProject() {
        return idProject; //todo Возможность переделать
    }

    public LocalDate GetCreatedAtServer() {
        return createdAtServer;
    }

    public LocalDateTime GetUpdatedAtServer() {
        return updatedAtServer;
    }

    public LocalDateTime GetDeadlineServer() {
        return deadlineServer;
    }

    public String GetUpdatedBy() {
        return updatedBy;
    }

    public LocalDateTime GetStatusChangeAtServer() {
        return statusChangeAtServer;
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

    public void SetUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void SetIdStatus(Integer idStatus) {
        this.idStatus = idStatus; //todo Возможность переделать
    }

    public void SetIdPriority(Integer idPriority) {
        this.idPriority = idPriority; //todo Возможность переделать
    }

    public void SetIdCreator(Integer idCreator) {
        this.idCreator = idCreator; //todo Возможность переделать
    }

    public void SetTimeSpent(Duration timeSpent) {
        this.timeSpent = timeSpent;
    }

    public void SetIdProject(Integer idProject) {
        this.idProject = idProject; //todo Возможность переделать
    }

    public void SetCreatedAtServer(LocalDate createdAtServer) {
        this.createdAtServer = createdAtServer;
    }

    public void SetUpdatedAtServer(LocalDateTime updatedAtServer) {
        this.updatedAtServer = updatedAtServer;
    }

    public void SetDeadlineServer(LocalDateTime deadlineServer) {
        this.deadlineServer = deadlineServer;
    }

    public void SetUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void SetStatusChangeAtServer(LocalDateTime statusChangeAtServer) {
        this.statusChangeAtServer = statusChangeAtServer;
    }
}
