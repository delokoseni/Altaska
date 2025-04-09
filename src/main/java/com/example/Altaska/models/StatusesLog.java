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

    @Column(name = "id_user", nullable = false)
    private Integer idUser; //todo Переделать

    @Column(name = "id_task", nullable = false)
    private Integer idTask; //todo Переделать

    public Long GetId() {
        return id;
    }

    public OffsetDateTime GetChangeAt() {
        return changeAt;
    }

    public Integer GetIdUser() {
        return idUser; //todo Возможно переделать
    }

    public Integer GetIdTask() {
        return idTask; //todo Возможно переделать
    }

    public void SetChangeAt(OffsetDateTime changeAt) {
        this.changeAt = changeAt;
    }

    public void SetIdUser(Integer idUser) {
        this.idUser = idUser; //todo Возможно переделать
    }

    public void SetIdTask(Integer idTask) {
        this.idTask = idTask; //todo Возможно переделать
    }
}
