package com.example.Altaska.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;

@Setter
@Getter
@Entity
@Table(name = "activity_log")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
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

}
