package com.example.Altaska.models;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Setter
@Getter
@Entity
@Table(name = "roles")
public class Roles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
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
}
