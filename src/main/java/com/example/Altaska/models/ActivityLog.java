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

    public Map<String, Object> GetDetails() {
        return details;
    }

    public OffsetDateTime GetActivityDate() {
        return activityDate;
    }

    public Users GetIdUser() {
        return idUser;
    }

    public Projects GetIdProject() {
        return idProject;
    }

    public OffsetDateTime GetActivityDateServer() {
        return activityDateServer;
    }

    public void SetDetails(Map<String, Object> details) {
        this.details = details;
    }

    public void SetActivityDate(OffsetDateTime activityDate) {
        this.activityDate = activityDate;
    }

    public void SetIdUser(Users idUser) {
        this.idUser = idUser;
    }

    public void SetIdProject(Projects idProject) {
        this.idProject = idProject;
    }

    public void SetActivityDateServer(OffsetDateTime activityDateServer) {
        this.activityDateServer = activityDateServer;
    }
}
