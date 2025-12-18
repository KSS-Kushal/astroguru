package com.kss.astrologer.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class PostCommentedEvent {
    private UUID postId;
    private UUID astrologerId;
    private String commenterUserName;
    private String comment;
}
