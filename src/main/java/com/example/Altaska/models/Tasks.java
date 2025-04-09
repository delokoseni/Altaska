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

    @ManyToOne
    @JoinColumn(name = "id_status", referencedColumnName = "id")
    private Statuses idStatus;

    @ManyToOne
    @JoinColumn(name = "id_priority")
    private Priorities idPriority;

    @ManyToOne
    @JoinColumn(name = "id_creator", referencedColumnName = "id")
    private Users idCreator;

    @Column(name = "time_spent")
    private Duration timeSpent;

    @ManyToOne
    @JoinColumn(name = "id_project", referencedColumnName = "id")
    private Projects idProject;

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

    public Statuses GetIdStatus() {
        return idStatus;
    }

    public Priorities GetIdPriority() {
        return idPriority;
    }

    public Users GetIdCreator() {
        return idCreator;
    }

    public Duration GetTimeSpent() {
        return timeSpent;
    }

    public Projects GetIdProject() {
        return idProject;
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

    public void SetIdStatus(Statuses idStatus) {
        this.idStatus = idStatus;
    }

    public void SetIdPriority(Priorities idPriority) {
        this.idPriority = idPriority;
    }

    public void SetIdCreator(Users idCreator) {
        this.idCreator = idCreator;
    }

    public void SetTimeSpent(Duration timeSpent) {
        this.timeSpent = timeSpent;
    }

    public void SetIdProject(Projects idProject) {
        this.idProject = idProject;
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
