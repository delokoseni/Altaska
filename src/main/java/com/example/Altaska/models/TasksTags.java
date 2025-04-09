package com.example.Altaska.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks_tags")
public class TasksTags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_tag", referencedColumnName = "id")
    private Tags idTag;

    @ManyToOne
    @JoinColumn(name = "id_task", referencedColumnName = "id")
    private Tasks idTask;

    @Column(name = "added_at_server", nullable = false)
    private LocalDateTime addedAtServer;

    public Long GetId() {
        return id;
    }

    public Tags GetIdTag() {
        return idTag;
    }

    public Tasks GetIdTask() {
        return idTask;
    }

    public LocalDateTime GetAddedAtServer() {
        return addedAtServer;
    }

    public void SetIdTag(Tags idTag) {
        this.idTag = idTag;
    }

    public void SetIdTask(Tasks idTask) {
        this.idTask = idTask;
    }

    public void SetAddedAtServer(LocalDateTime addedAtServer) {
        this.addedAtServer = addedAtServer;
    }
}
