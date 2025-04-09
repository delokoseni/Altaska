package com.example.Altaska.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "project_members")
public class ProjectMembers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_user", nullable = false)
    private Integer idUser; //todo Переделать

    @Column(name = "id_project", nullable = false)
    private Integer idProject; //todo Переделать

    @Column(name = "added_at", nullable = false)
    private LocalDate addedAt;

    @Column(name = "id_role", nullable = false)
    private Integer idRole; //todo Переделать

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

    public Integer GetIdUser() {
        return idUser;
    }

    public Integer GetIdProject() {
        return idProject; //todo Возможно переделать
    }

    public LocalDate GetAddedAt() {
        return addedAt;
    }

    public Integer GetIdRole() {
        return idRole; //todo Возможно переделать
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

    public void SetIdUser(Integer idUser) {
        this.idUser = idUser; //todo Возможно переделать
    }

    public void SetIdProject(Integer idProject) {
        this.idProject = idProject; //todo Возможно переделать
    }

    public void SetAddedAt(LocalDate addedAt) {
        this.addedAt = addedAt;
    }

    public void SetIdRole(Integer idRole) {
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
