package com.example.Altaska.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "subtasks")
public class SubTasks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
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
}
