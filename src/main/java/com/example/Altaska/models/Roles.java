package com.example.Altaska.models;

import jakarta.persistence.*;
import java.util.Map;

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
    /*@Column(name = "permissions", nullable = false, columnDefinition = "json")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> permissions;*/

    @Column(name = "id_project")
    private Integer idProject; //todo Переделать

    public Long GetId() {
        return id;
    }

    public boolean GetIsCustom() {
        return isCustom;
    }

    public String GetName() {
        return name;
    }

    //todo Сделать
    /*
    public Map<String, Object> GetPermissions() {
        return permissions;
    }
    */

    public Integer GetIdProject() {
        return idProject; //todo Возможно переделать
    }

    public void SetIsCustom(boolean isCustom) {
        this.isCustom = isCustom;
    }

    public void SetName(String name) {
        this.name = name;
    }
    //todo Сделать
    /*
    public void SetPermissions(Map<String, Object> permissions) {
        this.permissions = permissions;
    }
     */

    public void SetIdProject(Integer idProject) {
        this.idProject = idProject; //todo Возможно переделать
    }
}
