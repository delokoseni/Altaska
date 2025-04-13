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

    public Long getId() {
        return id;
    }

    public OffsetDateTime getChangeAt() {
        return changeAt;
    }

    public Users getIdUser() {
        return idUser;
    }

    public Tasks getIdTask() {
        return idTask;
    }

    public void setChangeAt(OffsetDateTime changeAt) {
        this.changeAt = changeAt;
    }

    public void setIdUser(Users idUser) {
        this.idUser = idUser;
    }

    public void setIdTask(Tasks idTask) {
        this.idTask = idTask;
    }
}
