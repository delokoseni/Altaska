package com.example.Altaska.controller;

import com.example.Altaska.models.*;
import com.example.Altaska.repositories.AttachmentsRepository;
import com.example.Altaska.repositories.TaskPerformersRepository;
import com.example.Altaska.repositories.TasksRepository;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.security.CustomUserDetails;
import com.example.Altaska.services.ActivityLogService;
import com.example.Altaska.services.NotificationService;
import com.example.Altaska.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
public class AttachmentsApiController {

    @Autowired
    private AttachmentsRepository attachmentsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TaskPerformersRepository taskPerformersRepository;

    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getFilesByTask(@PathVariable Long taskId) {
        List<Attachments> files = attachmentsRepository.findByIdTask_Id(taskId);
        if (files.isEmpty()) {
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
        Projects project = task.getIdProject();
        if (project == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("У задачи не указан проект");
        }
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        long maxFileSize = 25L * 1024 * 1024; // 25MB

        if (fileName == null || fileName.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Имя файла не может быть пустым");
        }

        if (fileName.length() > 255) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Имя файла слишком длинное (макс 255 символов)");
        }

        List<String> allowedMimeTypes = List.of(
                "image/png",
                "image/jpeg",
                "application/pdf",
                "text/plain",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );

        if (contentType == null || !allowedMimeTypes.contains(contentType)) {
            return ResponseEntity.badRequest().body("Недопустимый тип файла: " + contentType);
        }
        permissionService.checkIfProjectArchived(project);
        if (!permissionService.hasPermission(user.getId(), project.getId(), "attach_task_files")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
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
            activityLogService.logActivity(
                    user,
                    project,
                    "create",
                    "task_attachment",
                    attachment.getId(),
                    null,
                    "Файл \"" + file.getOriginalFilename() + "\" прикреплён к задаче: \"" + task.getName() + "\""
            );
            List<TaskPerformers> performers = taskPerformersRepository.findByIdTaskId(task.getId());
            Set<Users> recipients = performers.stream()
                    .map(TaskPerformers::getIdUser)
                    .filter(u -> !u.getId().equals(user.getId()))
                    .collect(Collectors.toSet());

            Users creator = task.getIdCreator();
            if (!creator.getId().equals(user.getId())) {
                recipients.add(creator);
            }

            if (!recipients.isEmpty()) {
                String message = "Пользователь " + user.getEmail() +
                        " прикрепил файл \"" + file.getOriginalFilename() + "\" к задаче \"" + task.getName() + "\"";

                notificationService.notifyUsers(
                        recipients,
                        "task_attachment",
                        "tasks",
                        task.getId(),
                        user,
                        message
                );
            }
            return ResponseEntity.ok(Map.of("message", "Файл успешно загружен"));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при загрузке файла");
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId, Principal principal) {
        Attachments attachment = attachmentsRepository.findById(fileId).orElse(null);
        if (attachment == null) {
            return ResponseEntity.notFound().build();
        }
        Users user = usersRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Tasks task = tasksRepository.findById(attachment.getIdTask().getId()).orElse(null);
        if (task == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        Projects project = task.getIdProject();
        if (project == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        permissionService.checkIfProjectArchived(project);
        if (!permissionService.hasPermission(user.getId(), project.getId(), "download_task_files")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав.");
        }
        activityLogService.logActivity(
                user,
                project,
                "read",
                "task_attachment",
                attachment.getId(),
                null,
                "Файл \"" + attachment.getFileName() + "\" скачан из задачи: \"" + task.getName() + "\""
        );

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
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не аутентифицирован.");
        }
        Long currentUserId = userDetails.GetUserId();
        if (!file.getIdUser().getId().equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы не можете удалить этот файл.");
        }
        Tasks task = file.getIdTask();
        if (task == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Файл не привязан к задаче.");
        }
        Projects project = task.getIdProject();
        if (project == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Задача не привязана к проекту.");
        }
        permissionService.checkIfProjectArchived(project);
        if (!permissionService.hasPermission(currentUserId, project.getId(), "delete_task_files")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        attachmentsRepository.delete(file);
        Users currentUser = usersRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            activityLogService.logActivity(
                    currentUser,
                    project,
                    "delete",
                    "task_attachment",
                    file.getId(),
                    null,
                    "Файл \"" + file.getFileName() + "\" удалён из задачи: \"" + task.getName() + "\""
            );
        }
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
