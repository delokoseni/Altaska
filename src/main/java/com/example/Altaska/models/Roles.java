package com.example.Altaska.models;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.util.Map;
import com.example.Altaska.converters.JsonConverter;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.Type;

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

    @Type(JsonType.class)
    @Column(name = "permissions", columnDefinition = "json", nullable = false)
    private JsonNode permissions;

    @ManyToOne
    @JoinColumn(name = "id_project", referencedColumnName = "id")
    private Projects idProject;

    public Long getId() {
        return id;
    }

    public boolean getIsCustom() {
        return isCustom;
    }

    public String getName() {
        return name;
    }

    public JsonNode getPermissions() {
        return permissions;
    }


    public Projects getIdProject() {
        return idProject;
    }

    public void setIsCustom(boolean isCustom) {
        this.isCustom = isCustom;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPermissions(JsonNode permissions) {
        this.permissions = permissions;
    }

    public void setIdProject(Projects idProject) {
        this.idProject = idProject;
    }
}
