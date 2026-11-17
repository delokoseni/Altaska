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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentsRepository commentsRepository;

    private final UsersRepository usersRepository;

    private final TasksRepository tasksRepository;

    private final PermissionService permissionService;

    private final ActivityLogService activityLogService;

    private final NotificationService notificationService;

    private final TaskPerformersRepository taskPerformersRepository;

    private static final String TASK_COMMENT = "task_comment";
    private static final String TASKS = "tasks";

    private static final String ACTION_ADD = "add";
    private static final String ACTION_EDIT = "edit";
    private static final String ACTION_DELETE = "delete";

    private static final String CONTENT_FIELD = "content";

    private static final String ERROR_INSUFFICIENT_PERMISSIONS = "Недостаточно прав.";
    private static final String ERROR_PROJECT_ARCHIVED = "Проект архивирован и не может быть изменён.";
    private static final String ERROR_TASK_NOT_FOUND = "Задача не найдена.";
    private static final String ERROR_TASK_MISSING = "Задача отсутствует.";

    @GetMapping("/task/{taskId}")
    public List<CommentDto> getCommentsByTask(@PathVariable Long taskId) {
        List<Comments> comments = commentsRepository.findByIdTask_Id(taskId);
        return comments.stream()
                .sorted(Comparator.comparing(Comments::getCreatedAtServer))
                .map(comment -> new CommentDto(
                        comment.getId(),
                        comment.getIdUser().getEmail(),
                        comment.getContent(),
                        comment.getCreatedAtServer().toString()
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
            return ResponseEntity.badRequest().body(ERROR_TASK_NOT_FOUND);
        }

        Projects project = task.getIdProject();
        if(permissionService.checkIfProjectArchived(project))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ERROR_PROJECT_ARCHIVED);

        if (!permissionService.hasPermission(user.getId(), project.getId(), "write_task_comments")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ERROR_INSUFFICIENT_PERMISSIONS);
        }

        Comments comment = new Comments();
        comment.setContent(commentRequest.getContent());
        OffsetDateTime odt = OffsetDateTime.now(ZoneId.systemDefault());
        comment.setCreatedAt(odt);
        comment.setCreatedAtServer(odt);
        comment.setIdTask(task);
        comment.setIdUser(user);

        commentsRepository.save(comment);
        List<Change> changes = List.of(
                new Change(CONTENT_FIELD, null, comment.getContent())
        );

        activityLogService.logActivity(
                user,
                project,
                ACTION_ADD,
                TASK_COMMENT,
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
                    TASK_COMMENT,
                    TASKS,
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Tasks task = tasksRepository.findById(comment.getIdTask().getId()).orElse(null);
        if (task == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ERROR_TASK_MISSING);
        }
        Projects project = task.getIdProject();
        if(permissionService.checkIfProjectArchived(project))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ERROR_PROJECT_ARCHIVED);

        if (!permissionService.hasPermission(user.getId(), project.getId(), "edit_task_comments")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ERROR_INSUFFICIENT_PERMISSIONS);
        }
        if (content.startsWith("\"") && content.endsWith("\"")) {
            content = content.substring(1, content.length() - 1);
        }
        String oldContent = comment.getContent();
        comment.setContent(content);
        commentsRepository.save(comment);

        activityLogService.logActivity(
                user,
                project,
                ACTION_EDIT,
                TASK_COMMENT,
                comment.getId(),
                List.of(new Change(CONTENT_FIELD, oldContent, content)),
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Tasks task = tasksRepository.findById(comment.getIdTask().getId()).orElse(null);
        if (task == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ERROR_TASK_MISSING);
        }
        Projects project = task.getIdProject();
        if(permissionService.checkIfProjectArchived(project))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ERROR_PROJECT_ARCHIVED);

        if (!permissionService.hasPermission(user.getId(), project.getId(), "delete_task_comments")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ERROR_INSUFFICIENT_PERMISSIONS);
        }
        String oldContent = comment.getContent();
        commentsRepository.delete(comment);
        List<Change> changes = List.of(
                new Change(CONTENT_FIELD, oldContent, null)
        );

        activityLogService.logActivity(
                user,
                project,
                ACTION_DELETE,
                TASK_COMMENT,
                comment.getId(),
                changes,
                "Комментарий удалён из задачи: \"" + task.getName() + "\""
        );
        return ResponseEntity.ok().build();
    }

    @Setter
    @Getter
    public static class CommentRequest {
        private Long taskId;
        private String content;
    }

    public record CommentDto(
            Long id,
            String authorName,
            String text,
            String createdAt) { }
}