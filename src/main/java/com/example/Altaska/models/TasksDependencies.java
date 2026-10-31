package com.example.Altaska.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "tasks_dependencies")
public class TasksDependencies {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @ManyToOne
    @JoinColumn(name = "id_from_task", referencedColumnName = "id")
    private Tasks idFromTask;

    @ManyToOne
    @JoinColumn(name = "id_to_task", referencedColumnName = "id")
    private Tasks idToTask;

    @Column(name = "lag")
    private Double lag;
}
