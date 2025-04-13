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

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Projects getIdProject() {
        return idProject;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIdProject(Projects idProject) {
        this.idProject = idProject;
    }
}
