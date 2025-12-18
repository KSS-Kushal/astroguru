package com.kss.astrologer.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class PostCreatedEvent {
    private UUID postId;
    private UUID userId;
}
