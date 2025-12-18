package com.kss.astrologer.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class PostLikedEvent {
    private UUID postId;
    private UUID astrologerId;
    private String likedUserName;
}
