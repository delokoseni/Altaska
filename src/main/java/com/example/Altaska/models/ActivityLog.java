package com.example.Altaska.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;

@Entity
@Table(name = "activity_log")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Type(JsonType.class)
    @Column(name = "details", columnDefinition = "json", nullable = false)
    private JsonNode details;

    @Column(name = "activity_date", nullable = false)
    private OffsetDateTime activityDate;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private Users idUser;

    @ManyToOne
    @JoinColumn(name = "id_project", referencedColumnName = "id")
    private Projects idProject;

    @Column(name = "activity_date_server", nullable = false)
    private OffsetDateTime activityDateServer;

    public Long GetId() {
        return id;
    }

    public JsonNode getDetails() {
        return details;
    }

    public OffsetDateTime getActivityDate() {
        return activityDate;
    }

    public Users getIdUser() {
        return idUser;
    }

    public Projects getIdProject() {
        return idProject;
    }

    public OffsetDateTime getActivityDateServer() {
        return activityDateServer;
    }

    public void setDetails(JsonNode details) {
        this.details = details;
    }

    public void setActivityDate(OffsetDateTime activityDate) {
        this.activityDate = activityDate;
    }

    public void setIdUser(Users idUser) {
        this.idUser = idUser;
    }

    public void setIdProject(Projects idProject) {
        this.idProject = idProject;
    }

    public void setActivityDateServer(OffsetDateTime activityDateServer) {
        this.activityDateServer = activityDateServer;
    }
}
