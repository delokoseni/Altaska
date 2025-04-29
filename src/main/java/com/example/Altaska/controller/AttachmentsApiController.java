package com.example.Altaska.controller;

import com.example.Altaska.models.Attachments;
import com.example.Altaska.models.Tasks;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.AttachmentsRepository;
import com.example.Altaska.repositories.TasksRepository;
import com.example.Altaska.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class AttachmentsApiController {

    @Autowired
    private AttachmentsRepository attachmentsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TasksRepository tasksRepository;

    // Получить список файлов для задачи
    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getFilesByTask(@PathVariable Long taskId) {
        List<Attachments> files = attachmentsRepository.findByIdTask_Id(taskId);
        if (files.isEmpty()) {
            // Возвращаем пустой массив, а не текст!
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(files.stream()
                .map(file -> new FileDto(
                        file.getId(),
                        file.getFileName(),
                        file.getFileType(),
                        file.getUploadedAt().toString()
                ))
                .toList());
    }

    // Загрузка файла
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("taskId") Long taskId,
                                        Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Users user = usersRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Tasks task = tasksRepository.findById(taskId).orElse(null);
        if (task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Задача не найдена");
        }

        try {
            Attachments attachment = new Attachments();
            attachment.setUploadedAt(OffsetDateTime.now());
            attachment.setUploadedAtServer(OffsetDateTime.now());
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileType(file.getContentType());
            attachment.setUploadedFile(file.getBytes());
            attachment.setIdTask(task);
            attachment.setIdUser(user);

            attachmentsRepository.save(attachment);

            return ResponseEntity.ok(Map.of("message", "Файл успешно загружен"));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при загрузке файла");
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        Attachments attachment = attachmentsRepository.findById(fileId).orElse(null);
        if (attachment == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + attachment.getFileName() + "\"")
                .header("Content-Type", attachment.getFileType())
                .body(attachment.getUploadedFile());
    }

    // DTO для ответа на запрос о файлах
    public static class FileDto {
        private Long id;
        private String fileName;
        private String fileType;
        private String uploadedAt;

        public FileDto(Long id, String fileName, String fileType, String uploadedAt) {
            this.id = id;
            this.fileName = fileName;
            this.fileType = fileType;
            this.uploadedAt = uploadedAt;
        }

        // Геттеры
        public Long getId() {
            return id;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFileType() {
            return fileType;
        }

        public String getUploadedAt() {
            return uploadedAt;
        }
    }
}
