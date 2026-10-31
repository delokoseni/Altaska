package com.example.Altaska.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

@Setter
@Getter
@Entity
@Table(name = "projects")
public class Projects {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
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
}
