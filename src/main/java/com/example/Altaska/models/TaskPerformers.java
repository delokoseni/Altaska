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

    public Long GetId() {
        return id;
    }

    public Tasks GetIdTask() {
        return idTask;
    }

    public Users GetIdUser() {
        return idUser;
    }

    public OffsetDateTime GetAddedAtServer() {
        return addedAtServer;
    }

    public void SetIdTask(Tasks idTask) {
        this.idTask = idTask;
    }

    public void SetIdUser(Users idUser) {
        this.idUser = idUser;
    }

    public void SetAddedAtServer(OffsetDateTime addedAtServer) {
        this.addedAtServer = addedAtServer;
    }
}
