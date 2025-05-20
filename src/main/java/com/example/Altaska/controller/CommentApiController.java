package com.example.Altaska.controller;

import com.example.Altaska.dto.Change;
import com.example.Altaska.models.*;
import com.example.Altaska.repositories.CommentsRepository;
import com.example.Altaska.repositories.TaskPerformersRepository;
import com.example.Altaska.repositories.TasksRepository;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.services.ActivityLogService;
import com.example.Altaska.services.NotificationService;
import com.example.Altaska.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentApiController {

    @Autowired
    private final CommentsRepository commentsRepository;

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

    @Autowired
    public CommentApiController(CommentsRepository commentsRepository) {
        this.commentsRepository = commentsRepository;
    }

    @GetMapping("/task/{taskId}")
    public List<CommentDto> getCommentsByTask(@PathVariable Long taskId) {
        List<Comments> comments = commentsRepository.findByIdTask_Id(taskId);
        return comments.stream()
                .sorted(Comparator.comparing(Comments::getCreatedAtServer))  // Сортировка по возрастанию
                .map(comment -> new CommentDto(
                        comment.getId(),
                        comment.getIdUser().getEmail(),
                        comment.getContent()
                ))
                .toList();
    }

    @PostMapping("/add-comment")
    public ResponseEntity<?> addComment(@RequestBody CommentRequest commentRequest, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Users user = usersRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Tasks task = tasksRepository.findById(commentRequest.getTaskId()).orElse(null);
        if (task == null) {
            return ResponseEntity.badRequest().body("Задача не найдена");
        }

        Projects project = task.getIdProject();
        permissionService.checkIfProjectArchived(project);
        if (!permissionService.hasPermission(user.getId(), project.getId(), "write_task_comments")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }

        Comments comment = new Comments();
        comment.setContent(commentRequest.getContent());
        comment.setCreatedAt(OffsetDateTime.now()); //TODO Изменить
        comment.setCreatedAtServer(OffsetDateTime.now());
        comment.setIdTask(task);
        comment.setIdUser(user);

        commentsRepository.save(comment);
        List<Change> changes = List.of(
                new Change("content", null, comment.getContent())
        );

        activityLogService.logActivity(
                user,
                project,
                "add",
                "task_comment",
                comment.getId(),
                changes,
                "Добавлен комментарий к задаче: \"" + task.getName() + "\""
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
                    " добавил комментарий к задаче \"" + task.getName() + "\"";

            notificationService.notifyUsers(
                    recipients,
                    "task_comment",
                    "tasks",
                    task.getId(),
                    user,
                    message
            );
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/edit-comment/{commentId}")
    public ResponseEntity<?> editComment(@PathVariable Long commentId, @RequestBody String content, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Users user = usersRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Comments comment = commentsRepository.findById(commentId).orElse(null);
        if (comment == null || !comment.getIdUser().equals(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Можно редактировать только свои комментарии
        }
        Tasks task = tasksRepository.findById(comment.getIdTask().getId()).orElse(null);
        Projects project = task.getIdProject();
        permissionService.checkIfProjectArchived(project);
        if (!permissionService.hasPermission(user.getId(), project.getId(), "edit_task_comments")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        if (content.startsWith("\"") && content.endsWith("\"")) {
            content = content.substring(1, content.length() - 1);
        }
        String oldContent = comment.getContent();
        comment.setContent(content);
        commentsRepository.save(comment);
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("content", oldContent);

        activityLogService.logActivity(
                user,
                project,
                "edit",
                "task_comment",
                comment.getId(),
                List.of(new Change("content", oldContent, content)),
                "Комментарий к задаче \"" + task.getName() + "\" изменён"
        );
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-comment/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Users user = usersRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Comments comment = commentsRepository.findById(commentId).orElse(null);
        if (comment == null || !comment.getIdUser().equals(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Можно удалять только свои комментарии
        }
        Tasks task = tasksRepository.findById(comment.getIdTask().getId()).orElse(null);
        Projects project = task.getIdProject();
        permissionService.checkIfProjectArchived(project);
        if (!permissionService.hasPermission(user.getId(), project.getId(), "delete_task_comments")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Недостаточно прав.");
        }
        String oldContent = comment.getContent();
        commentsRepository.delete(comment);
        List<Change> changes = List.of(
                new Change("content", oldContent, null)
        );

        activityLogService.logActivity(
                user,
                project,
                "delete",
                "task_comment",
                comment.getId(),
                changes,
                "Комментарий удалён из задачи: \"" + task.getName() + "\""
        );
        return ResponseEntity.ok().build();
    }

    // DTO для запроса
    public static class CommentRequest {
        private Long taskId;
        private String content;

        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class CommentDto {
        private Long id; // ID комментария
        private String authorName;
        private String text;

        public CommentDto(Long id, String authorName, String text) {
            this.id = id;
            this.authorName = authorName;
            this.text = text;
        }

        // Геттеры
        public Long getId() {
            return id;
        }

        public String getAuthorName() {
            return authorName;
        }

        public String getText() {
            return text;
        }
    }
}