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

    public Long getId() {
        return id;
    }

    public Users getIdUser() {
        return idUser;
    }

    public Projects getIdProject() {
        return idProject;
    }

    public LocalDate getAddedAt() {
        return addedAt;
    }

    public Roles getIdRole() {
        return idRole;
    }

    public boolean getConfirmed() {
        return confirmed;
    }

    public String getConfirmationToken() {
        return confirmationToken;
    }

    public LocalDate getAddedAtServer() {
        return addedAtServer;
    }

    public String getInvitedBy() {
        return invitedBy;
    }

    public void setIdUser(Users idUser) {
        this.idUser = idUser;
    }

    public void setIdProject(Projects idProject) {
        this.idProject = idProject;
    }

    public void setAddedAt(LocalDate addedAt) {
        this.addedAt = addedAt;
    }

    public void setIdRole(Roles idRole) {
        this.idRole = idRole;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public void setConfirmationToken(String confirmationToken) {
        this.confirmationToken = confirmationToken;
    }

    public void setAddedAtServer(LocalDate addedAtServer) {
        this.addedAtServer = addedAtServer;
    }

    public void setInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
    }
}
