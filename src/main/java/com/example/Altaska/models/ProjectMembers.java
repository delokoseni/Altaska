package com.example.Altaska.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "project_members")
public class ProjectMembers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public Long GetId() {
        return id;
    }

    public Users GetIdUser() {
        return idUser;
    }

    public Projects GetIdProject() {
        return idProject;
    }

    public LocalDate GetAddedAt() {
        return addedAt;
    }

    public Roles GetIdRole() {
        return idRole;
    }

    public boolean GetConfirmed() {
        return confirmed;
    }

    public String GetConfirmationToken() {
        return confirmationToken;
    }

    public LocalDate GetAddedAtServer() {
        return addedAtServer;
    }

    public String GetInvitedBy() {
        return invitedBy;
    }

    public void SetIdUser(Users idUser) {
        this.idUser = idUser;
    }

    public void SetIdProject(Projects idProject) {
        this.idProject = idProject;
    }

    public void SetAddedAt(LocalDate addedAt) {
        this.addedAt = addedAt;
    }

    public void SetIdRole(Roles idRole) {
        this.idRole = idRole;
    }

    public void SetConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public void SetConfirmationToken(String confirmationToken) {
        this.confirmationToken = confirmationToken;
    }

    public void SetAddedAtServer(LocalDate addedAtServer) {
        this.addedAtServer = addedAtServer;
    }

    public void SetInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
    }
}
