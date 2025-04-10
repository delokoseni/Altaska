package com.example.Altaska.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

@Entity
@Table(name = "projects")
public class Projects {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @Column(name = "description", length = 1500)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived;

    @Column(name = "created_at_server", nullable = false)
    private LocalDate createdAtServer;

    @Column(name = "updated_at_server", nullable = false)
    private OffsetTime updatedAtServer;

    @Column(name = "updated_by", length = 254, nullable = false)
    private String updatedBy;

    @ManyToOne
    @JoinColumn(name = "id_owner", referencedColumnName = "id")
    private Users idOwner;

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

    public boolean GetIsArchived() {
        return isArchived;
    }

    public LocalDate GetCreatedAtServer() {
        return createdAtServer;
    }

    public OffsetTime GetUpdatedAtServer() {
        return updatedAtServer;
    }

    public String GetUpdatedBy() {
        return updatedBy;
    }

    public Users GetIdOwner() {
        return idOwner;
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

    public void SetIsArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }

    public void SetCreatedAtServer(LocalDate createdAtServer) {
        this.createdAtServer = createdAtServer;
    }

    public void SetUpdatedAtServer(OffsetTime updatedAtServer) {
        this.updatedAtServer = updatedAtServer;
    }

    public void SetUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void SetIdOwner(Users idOwner) {
        this.idOwner = idOwner;
    }
}
