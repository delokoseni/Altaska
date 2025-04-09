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

    @ManyToOne
    @JoinColumn(name = "id_project", referencedColumnName = "id")
    private Projects idProject;

    public Long GetId() {
        return id;
    }

    public String GetName() {
        return name;
    }

    public Projects GetIdProject() {
        return idProject;
    }

    public void SetName(String name) {
        this.name = name;
    }

    public void SetIdProject(Projects idProject) {
        this.idProject = idProject;
    }
}
