package com.kss.astrologer.dto;

import com.kss.astrologer.models.Post;
import com.kss.astrologer.models.PostImage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {
    private UUID id;
    private AstrologerDto astrologer;
    private String text;
    private List<PostImage> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long likeCount;
    private long commentCount;

    public PostDto(Post post) {
        this.id = post.getId();
        this.astrologer = new AstrologerDto(post.getAstrologer());
        this.text = post.getText();
        this.images = post.getImages();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.likeCount = 0L;
        this.commentCount = 0L;
    }
}
