package com.example.Altaska.models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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
    private Long timeSpent;

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

    @Column(name = "start_time_server")
    private LocalDateTime startTimeServer;

    public Long getId() {
        return id;
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

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Statuses getIdStatus() {
        return idStatus;
    }

    public Priorities getIdPriority() {
        return idPriority;
    }

    public Users getIdCreator() {
        return idCreator;
    }

    public Long getTimeSpent() {
        return timeSpent;
    }

    public Projects getIdProject() {
        return idProject;
    }

    public LocalDate getCreatedAtServer() {
        return createdAtServer;
    }

    public LocalDateTime getUpdatedAtServer() {
        return updatedAtServer;
    }

    public LocalDateTime getDeadlineServer() {
        return deadlineServer;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public LocalDateTime getStatusChangeAtServer() {
        return statusChangeAtServer;
    }

    public LocalDateTime getStartTimeServer() {
        return startTimeServer;
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

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setIdStatus(Statuses idStatus) {
        this.idStatus = idStatus;
    }

    public void setIdPriority(Priorities idPriority) {
        this.idPriority = idPriority;
    }

    public void setIdCreator(Users idCreator) {
        this.idCreator = idCreator;
    }

    public void setTimeSpent(Long timeSpent) {
        this.timeSpent = timeSpent;
    }

    public void setIdProject(Projects idProject) {
        this.idProject = idProject;
    }

    public void setCreatedAtServer(LocalDate createdAtServer) {
        this.createdAtServer = createdAtServer;
    }

    public void setUpdatedAtServer(LocalDateTime updatedAtServer) {
        this.updatedAtServer = updatedAtServer;
    }

    public void setDeadlineServer(LocalDateTime deadlineServer) {
        this.deadlineServer = deadlineServer;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setStatusChangeAtServer(LocalDateTime statusChangeAtServer) {
        this.statusChangeAtServer = statusChangeAtServer;
    }

    public void setStartTimeServer(LocalDateTime startTimeServer) {
        this.startTimeServer = startTimeServer;
    }
}
