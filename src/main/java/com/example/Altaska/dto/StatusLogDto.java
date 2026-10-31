package com.example.Altaska.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class StatusLogDto {
    private OffsetDateTime timestamp;
    private String user;
    private String status;
}
