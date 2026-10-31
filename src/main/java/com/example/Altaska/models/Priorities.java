package com.example.Altaska.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "priorities")
public class Priorities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Column(name = "level", nullable = false)
    private Long level;

}
