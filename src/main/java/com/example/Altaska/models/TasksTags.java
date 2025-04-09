package com.example.Altaska.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks_tags")
public class TasksTags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_tag", nullable = false)
    private Integer idTag; //todo Переделать

    @Column(name = "id_task", nullable = false)
    private Integer idTask; //todo Переделать

    @Column(name = "added_at_server", nullable = false)
    private LocalDateTime addedAtServer;

    public Long GetId() {
        return id;
    }

    public Integer GetIdTag() {
        return idTag; //todo Возможно переделать
    }

    public Integer GetIdTask() {
        return idTask; //todo Возможно переделать
    }

    public LocalDateTime GetAddedAtServer() {
        return addedAtServer;
    }

    public void SetIdTag(Integer idTag) {
        this.idTag = idTag; //todo Возможно переделать
    }

    public void SetIdTask(Integer idTask) {
        this.idTask = idTask; //todo Возможно переделать
    }

    public void SetAddedAtServer(LocalDateTime addedAtServer) {
        this.addedAtServer = addedAtServer;
    }
}
