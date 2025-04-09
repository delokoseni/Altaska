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

    @Column(name = "id_task", nullable = false)
    private Integer idTask; //todo Переделать

    @Column(name = "id_user", nullable = false)
    private Integer idUser; //todo Переделать

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

    public Integer GetIdTask() {
        return idTask; //todo Возможно переделать
    }

    public Integer GetIdUser() {
        return idUser; //todo Возможно переделать
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

    public void SetIdTask(Integer idTask) {
        this.idTask = idTask; //todo Возможно переделать
    }

    public void SetIdUser(Integer idUser) {
        this.idUser = idUser; //todo Возможно переделать
    }

    public void SetUploadedAtServer(OffsetDateTime uploadedAtServer) {
        this.uploadedAtServer = uploadedAtServer;
    }
}
