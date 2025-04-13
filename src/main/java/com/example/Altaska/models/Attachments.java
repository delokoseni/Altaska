package com.example.Altaska.models;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "attachments")
public class Attachments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public Long getId() {
        return id;
    }

    public OffsetDateTime getUploadedAt() {
        return uploadedAt;
    }

    public byte[] getUploadedFile() {
        return uploadedFile;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public Tasks getIdTask() {
        return idTask;
    }

    public Users getIdUser() {
        return idUser;
    }

    public OffsetDateTime getUploadedAtServer() {
        return uploadedAtServer;
    }

    public void setUploadedAt(OffsetDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public void setUploadedFile(byte[] uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setIdTask(Tasks idTask) {
        this.idTask = idTask;
    }

    public void setIdUser(Users idUser) {
        this.idUser = idUser;
    }

    public void setUploadedAtServer(OffsetDateTime uploadedAtServer) {
        this.uploadedAtServer = uploadedAtServer;
    }
}
