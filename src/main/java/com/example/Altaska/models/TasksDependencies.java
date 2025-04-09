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

    @Column(name = "id_from_task", nullable = false)
    private Integer idFromTask; //todo Переделать

    @Column(name = "id_to_task", nullable = false)
    private Integer idToTask; //todo Переделать

    @Column(name = "sort_order")
    private Integer sortOrder;

    public Long GetId() {
        return id;
    }

    public String GetType() {
        return type;
    }

    public Integer GetIdFromTask() {
        return idFromTask; //todo Возможно переделать
    }

    public Integer GetIdToTask() {
        return idToTask; //todo Возможно переделать
    }

    public Integer GetSortOrder() {
        return sortOrder;
    }

    public void SetType(String type) {
        this.type = type;
    }

    public void SetIdFromTask(Integer idFromTask) {
        this.idFromTask = idFromTask; //todo Возможно переделать
    }

    public void SetIdToTask(Integer idToTask) {
        this.idToTask = idToTask; //todo Возможно переделать
    }

    public void SetSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
