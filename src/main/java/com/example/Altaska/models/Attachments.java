package com.example.Altaska.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Setter
@Getter
@Entity
@Table(name = "attachments")
public class Attachments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;

    @Column(name = "uploaded_file", nullable = false)
    private byte[] uploadedFile;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "file_type", length = 50, nullable = false)
    private String fileType;

    @ManyToOne
    @JoinColumn(name = "id_task", referencedColumnName = "id")
    private Tasks idTask;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private Users idUser;

    @Column(name = "uploaded_at_server", nullable = false)
    private OffsetDateTime uploadedAtServer;

}
