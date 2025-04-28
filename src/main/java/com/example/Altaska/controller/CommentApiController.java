package com.example.Altaska.controller;

import com.example.Altaska.models.Comments;
import com.example.Altaska.repositories.CommentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentApiController {

    private final CommentsRepository commentsRepository;

    @Autowired
    public CommentApiController(CommentsRepository commentsRepository) {
        this.commentsRepository = commentsRepository;
    }

    @GetMapping("/task/{taskId}")
    public List<CommentDto> getCommentsByTask(@PathVariable Long taskId) {
        List<Comments> comments = commentsRepository.findByIdTask_Id(taskId);
        return comments.stream()
                .map(comment -> new CommentDto(
                        comment.getIdUser().getEmail(),
                        comment.getContent()
                ))
                .toList();
    }

    // DTO класс, чтобы на фронт отправлять только нужные данные
    public static class CommentDto {
        private String authorName;
        private String text;

        public CommentDto(String authorName, String text) {
            this.authorName = authorName;
            this.text = text;
        }

        public String getAuthorName() {
            return authorName;
        }

        public String getText() {
            return text;
        }
    }
}