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

    public Long getId() {
        return id;
    }

    public Tags getIdTag() {
        return idTag;
    }

    public Tasks getIdTask() {
        return idTask;
    }

    public LocalDateTime getAddedAtServer() {
        return addedAtServer;
    }

    public void setIdTag(Tags idTag) {
        this.idTag = idTag;
    }

    public void setIdTask(Tasks idTask) {
        this.idTask = idTask;
    }

    public void setAddedAtServer(LocalDateTime addedAtServer) {
        this.addedAtServer = addedAtServer;
    }
}
