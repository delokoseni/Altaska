package com.example.Altaska.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "tasks")
public class Tasks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
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
}
