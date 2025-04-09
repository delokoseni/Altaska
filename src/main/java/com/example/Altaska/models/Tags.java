package com.example.Altaska.models;

import jakarta.persistence.*;

@Entity
@Table(name = "tags")
public class Tags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Column(name = "id_project", nullable = false)
    private Integer idProject; //todo Переделать

    public Long GetId() {
        return id;
    }

    public String GetName() {
        return name;
    }

    public Integer GetIdProject() {
        return idProject; //todo Возможно переделать
    }

    public void SetName(String name) {
        this.name = name;
    }

    public void SetIdProject(Integer idProject) {
        this.idProject = idProject; //todo Возможно переделать
    }
}
