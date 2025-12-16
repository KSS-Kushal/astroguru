package com.kss.astrologer.dto;


import com.kss.astrologer.models.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private UUID id;
    private UserDto user;
    private UUID postId;
    private String body;
    private LocalDateTime createdAt;

    public CommentDTO(Comment comment) {
        this.id = comment.getId();
        this.user = new UserDto(comment.getUser());
        this.postId = comment.getPost().getId();
        this.body = comment.getBody();
        this.createdAt = comment.getCreatedAt();
    }
}
