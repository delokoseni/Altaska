package com.example.Altaska.models;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "task_performers")
public class TaskPerformers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_task", referencedColumnName = "id")
    private Tasks idTask;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private Users idUser;

    @Column(name = "added_at_server", nullable = false)
    private OffsetDateTime addedAtServer;

    public Long getId() {
        return id;
    }

    public Tasks getIdTask() {
        return idTask;
    }

    public Users getIdUser() {
        return idUser;
    }

    public OffsetDateTime getAddedAtServer() {
        return addedAtServer;
    }

    public void setIdTask(Tasks idTask) {
        this.idTask = idTask;
    }

    public void setIdUser(Users idUser) {
        this.idUser = idUser;
    }

    public void setAddedAtServer(OffsetDateTime addedAtServer) {
        this.addedAtServer = addedAtServer;
    }
}
