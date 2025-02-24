package com.example.Altaska.models;

import jakarta.persistence.*;

@Entity
@Table(
        name = "user_type"
)
public class UserType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", unique = true, nullable = false)
    private String type;

    public UserType() { }

    public Long GetId() { return this.id; }

    public String GetType() { return this.type; }
}
