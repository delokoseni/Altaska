package com.example.Altaska.models;

import jakarta.persistence.*;
import java.util.Map;
import com.example.Altaska.converters.JsonConverter;

@Entity
@Table(name = "roles")
public class Roles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_custom", nullable = false)
    private boolean isCustom;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    //todo Добавить конвертер для JSON и сделать
    @Column(name = "permissions", nullable = false, columnDefinition = "json")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> permissions;

    @ManyToOne
    @JoinColumn(name = "id_project", referencedColumnName = "id")
    private Projects idProject;

    public Long GetId() {
        return id;
    }

    public boolean GetIsCustom() {
        return isCustom;
    }

    public String GetName() {
        return name;
    }

    public Map<String, Object> GetPermissions() {
        return permissions;
    }


    public Projects GetIdProject() {
        return idProject;
    }

    public void SetIsCustom(boolean isCustom) {
        this.isCustom = isCustom;
    }

    public void SetName(String name) {
        this.name = name;
    }

    public void SetPermissions(Map<String, Object> permissions) {
        this.permissions = permissions;
    }

    public void SetIdProject(Projects idProject) {
        this.idProject = idProject;
    }
}
