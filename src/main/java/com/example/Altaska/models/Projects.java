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

    public boolean getIsArchived() {
        return isArchived;
    }

    public LocalDate getCreatedAtServer() {
        return createdAtServer;
    }

    public OffsetTime getUpdatedAtServer() {
        return updatedAtServer;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Users getIdOwner() {
        return idOwner;
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

    public void setIsArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }

    public void setCreatedAtServer(LocalDate createdAtServer) {
        this.createdAtServer = createdAtServer;
    }

    public void setUpdatedAtServer(OffsetTime updatedAtServer) {
        this.updatedAtServer = updatedAtServer;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setIdOwner(Users idOwner) {
        this.idOwner = idOwner;
    }
}
