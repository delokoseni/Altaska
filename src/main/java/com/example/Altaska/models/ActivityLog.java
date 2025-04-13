package com.example.Altaska.models;

import com.example.Altaska.converters.JsonConverter;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "activity_log")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "details", columnDefinition = "json", nullable = false)
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> details;

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

    public Map<String, Object> getDetails() {
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

    public void setDetails(Map<String, Object> details) {
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
