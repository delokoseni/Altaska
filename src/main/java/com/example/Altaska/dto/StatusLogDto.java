package com.example.Altaska.dto;

import java.time.OffsetDateTime;

public class StatusLogDto {
    private OffsetDateTime timestamp;
    private String user;
    private String status;

    public StatusLogDto(OffsetDateTime timestamp, String user, String status) {
        this.timestamp = timestamp;
        this.user = user;
        this.status = status;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public String getUser() {
        return user;
    }

    public String getStatus() {
        return status;
    }
}
