package com.example.Altaska.controller;

import com.example.Altaska.models.Attachments;
import com.example.Altaska.models.Tasks;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.AttachmentsRepository;
import com.example.Altaska.repositories.TasksRepository;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                        file.getUploadedAt().toString(),
                        file.getIdUser().getEmail()
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

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId, Authentication authentication) {
        Optional<Attachments> optionalAttachment = attachmentsRepository.findById(fileId);
        if (optionalAttachment.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Attachments file = optionalAttachment.get();

        // Получаем ID текущего пользователя
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не аутентифицирован.");
        }

        Long currentUserId = userDetails.GetUserId();

        // Проверка: только тот, кто загрузил файл, может его удалить
        if (!file.getIdUser().getId().equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы не можете удалить этот файл.");
        }

        attachmentsRepository.delete(file);
        return ResponseEntity.ok("Файл удален.");
    }



    // DTO для ответа на запрос о файлах
    public static class FileDto {
        private Long id;
        private String fileName;
        private String fileType;
        private String uploadedAt;
        private String uploadedByEmail;

        public FileDto(Long id, String fileName, String fileType, String uploadedAt, String uploadedByEmail) {
            this.id = id;
            this.fileName = fileName;
            this.fileType = fileType;
            this.uploadedAt = uploadedAt;
            this.uploadedByEmail = uploadedByEmail;
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

        public  String getUploadedByEmail() {
            return uploadedByEmail;
        }
    }
}
