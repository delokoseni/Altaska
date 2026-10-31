package com.example.Altaska.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Setter
@Getter
@Entity
@Table(name = "statuses_log")
public class StatusesLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "change_at", nullable = false)
    private OffsetDateTime changeAt;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private Users idUser;

    @ManyToOne
    @JoinColumn(name = "id_task", referencedColumnName = "id")
    private Tasks idTask;

    @Column(name = "new_status", nullable = false)
    private String newStatus;
}
