package com.example.Altaska.models;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "activity_log")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //todo Переделать
    /*
    @Column(name = "details", columnDefinition = "json", nullable = false)
    private String details;
     */

    @Column(name = "activity_date", nullable = false)
    private OffsetDateTime activityDate;

    @Column(name = "id_user", nullable = false)
    private Integer idUser; //todo Переделать

    @Column(name = "id_project", nullable = false)
    private Integer idProject; //todo Переделать

    @Column(name = "activity_date_server", nullable = false)
    private OffsetDateTime activityDateServer;

    public Long GetId() {
        return id;
    }

    //todo Переделать
    /*
    public String GetDetails() {
        return details;
    }
    */

    public OffsetDateTime GetActivityDate() {
        return activityDate;
    }

    public Integer GetIdUser() {
        return idUser; //todo Возможно переделать
    }

    public Integer GetIdProject() {
        return idProject; //todo Возможно переделать
    }

    public OffsetDateTime GetActivityDateServer() {
        return activityDateServer;
    }

    //todo Переделать
    /*
    public void SetDetails(String details) {
        this.details = details;
    }
    */

    public void SetActivityDate(OffsetDateTime activityDate) {
        this.activityDate = activityDate;
    }

    public void SetIdUser(Integer idUser) {
        this.idUser = idUser; //todo Переделать
    }

    public void SetIdProject(Integer idProject) {
        this.idProject = idProject; //todo Переделать
    }

    public void SetActivityDateServer(OffsetDateTime activityDateServer) {
        this.activityDateServer = activityDateServer;
    }
}
