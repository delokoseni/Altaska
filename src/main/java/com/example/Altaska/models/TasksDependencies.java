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

    @Column(name = "lag")
    private Double lag;

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Tasks getIdFromTask() {
        return idFromTask;
    }

    public Tasks getIdToTask() {
        return idToTask;
    }

    public Double getLag() {
        return lag;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setIdFromTask(Tasks idFromTask) {
        this.idFromTask = idFromTask;
    }

    public void setIdToTask(Tasks idToTask) {
        this.idToTask = idToTask;
    }

    public void setLag(Double lag) {
        this.lag = lag;
    }
}
