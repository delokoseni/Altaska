package com.example.Altaska.controller;

import com.example.Altaska.models.Comments;
import com.example.Altaska.models.Tasks;
import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.CommentsRepository;
import com.example.Altaska.repositories.TasksRepository;
import com.example.Altaska.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

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
    public CommentApiController(CommentsRepository commentsRepository) {
        this.commentsRepository = commentsRepository;
    }

    @GetMapping("/task/{taskId}")
    public List<CommentDto> getCommentsByTask(@PathVariable Long taskId) {
        List<Comments> comments = commentsRepository.findByIdTask_Id(taskId);

        // Сортируем комментарии по времени создания (createdAt) по возрастанию
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

        Comments comment = new Comments();
        comment.setContent(commentRequest.getContent());
        comment.setCreatedAt(OffsetDateTime.now()); //TODO Изменить
        comment.setCreatedAtServer(OffsetDateTime.now());
        comment.setIdTask(task);
        comment.setIdUser(user);

        commentsRepository.save(comment);

        return ResponseEntity.ok().build();
    }

    // Редактирование комментария
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
        if (content.startsWith("\"") && content.endsWith("\"")) {
            content = content.substring(1, content.length() - 1);
        }
        comment.setContent(content);
        commentsRepository.save(comment);

        return ResponseEntity.ok().build();
    }

    // Удаление комментария
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

        commentsRepository.delete(comment);

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