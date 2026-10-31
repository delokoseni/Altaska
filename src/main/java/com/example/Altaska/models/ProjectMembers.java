package com.example.Altaska.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "project_members")
public class ProjectMembers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private Users idUser;

    @ManyToOne
    @JoinColumn(name = "id_project",referencedColumnName = "id")
    private Projects idProject;

    @Column(name = "added_at", nullable = false)
    private LocalDate addedAt;

    @ManyToOne
    @JoinColumn(name = "id_role", referencedColumnName = "id")
    private Roles idRole;

    @Column(name = "confirmed", nullable = false)
    private boolean confirmed;

    @Column(name = "confirmation_token", length = 255)
    private String confirmationToken;

    @Column(name = "added_at_server", nullable = false)
    private LocalDate addedAtServer;

    @Column(name = "invited_by", length = 254, nullable = false)
    private String invitedBy;

    @Column(name = "inviteeEmail", length = 254, nullable = false)
    private String inviteeEmail;
}
