package com.kss.astrologer.dto;

import com.kss.astrologer.models.Like;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeDTO {
    private UUID id;
    private UserDto user;
    private UUID postId;
    private LocalDateTime createdAt;

    public LikeDTO(Like like) {
        this.id = like.getId();
        this.user = new UserDto(like.getUser());
        this.postId = like.getPost().getId();
        this.createdAt = like.getCreatedAt();
    }
}
