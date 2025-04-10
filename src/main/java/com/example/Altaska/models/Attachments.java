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

    public Long GetId() {
        return id;
    }

    public OffsetDateTime GetUploadedAt() {
        return uploadedAt;
    }

    public byte[] GetUploadedFile() {
        return uploadedFile;
    }

    public String GetFileName() {
        return fileName;
    }

    public String GetFileType() {
        return fileType;
    }

    public Tasks GetIdTask() {
        return idTask;
    }

    public Users GetIdUser() {
        return idUser;
    }

    public OffsetDateTime GetUploadedAtServer() {
        return uploadedAtServer;
    }

    public void SetUploadedAt(OffsetDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public void SetUploadedFile(byte[] uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public void SetFileName(String fileName) {
        this.fileName = fileName;
    }

    public void SetFileType(String fileType) {
        this.fileType = fileType;
    }

    public void SetIdTask(Tasks idTask) {
        this.idTask = idTask;
    }

    public void SetIdUser(Users idUser) {
        this.idUser = idUser;
    }

    public void SetUploadedAtServer(OffsetDateTime uploadedAtServer) {
        this.uploadedAtServer = uploadedAtServer;
    }
}
