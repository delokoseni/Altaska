package com.example.Altaska.models;

import jakarta.persistence.*;

@Entity
@Table(name = "tasks_dependencies")
public class TasksDependencies {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @ManyToOne
    @JoinColumn(name = "id_from_task", referencedColumnName = "id")
    private Tasks idFromTask;

    @ManyToOne
    @JoinColumn(name = "id_to_task", referencedColumnName = "id")
    private Tasks idToTask;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public Long GetId() {
        return id;
    }

    public String GetType() {
        return type;
    }

    public Tasks GetIdFromTask() {
        return idFromTask;
    }

    public Tasks GetIdToTask() {
        return idToTask;
    }

    public Integer GetSortOrder() {
        return sortOrder;
    }

    public void SetType(String type) {
        this.type = type;
    }

    public void SetIdFromTask(Tasks idFromTask) {
        this.idFromTask = idFromTask;
    }

    public void SetIdToTask(Tasks idToTask) {
        this.idToTask = idToTask;
    }

    public void SetSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
