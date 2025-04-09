package com.example.Altaska.models;

import jakarta.persistence.*;

@Entity
@Table(name = "priorities")
public class Priorities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Column(name = "level", nullable = false)
    private Long level;

    public Long GetId() {
        return id;
    }

    public String GetName() {
        return name;
    }

    public Long GetLevel() {
        return level;
    }

    public void SetName(String name) {
        this.name = name;
    }

    public void SetLevel(Long level) {
        this.level = level;
    }
}
