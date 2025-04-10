package com.example.Altaska.models;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "statuses_log")
public class StatusesLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "change_at", nullable = false)
    private OffsetDateTime changeAt;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private Users idUser;

    @ManyToOne
    @JoinColumn(name = "id_task", referencedColumnName = "id")
    private Tasks idTask;

    public Long GetId() {
        return id;
    }

    public OffsetDateTime GetChangeAt() {
        return changeAt;
    }

    public Users GetIdUser() {
        return idUser;
    }

    public Tasks GetIdTask() {
        return idTask;
    }

    public void SetChangeAt(OffsetDateTime changeAt) {
        this.changeAt = changeAt;
    }

    public void SetIdUser(Users idUser) {
        this.idUser = idUser;
    }

    public void SetIdTask(Tasks idTask) {
        this.idTask = idTask;
    }
}
