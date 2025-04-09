package com.example.Altaska.models;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "task_performers")
public class TaskPerformers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_task", nullable = false)
    private Integer idTask; //todo Переделать

    @Column(name = "id_user", nullable = false)
    private Integer idUser; //todo Переделать

    @Column(name = "added_at_server", nullable = false)
    private OffsetDateTime addedAtServer;

    public Long GetId() {
        return id;
    }

    public Integer GetIdTask() {
        return idTask; //todo Возможно переделать
    }

    public Integer GetIdUser() {
        return idUser; //todo Возможно переделать
    }

    public OffsetDateTime GetAddedAtServer() {
        return addedAtServer;
    }

    public void SetIdTask(Integer idTask) {
        this.idTask = idTask; //todo Возможно переделать
    }

    public void SetIdUser(Integer idUser) {
        this.idUser = idUser; //todo Возможно переделать
    }

    public void SetAddedAtServer(OffsetDateTime addedAtServer) {
        this.addedAtServer = addedAtServer;
    }
}
