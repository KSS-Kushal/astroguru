package com.kss.astrologer.events;

import com.kss.astrologer.dto.ChatSessionDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class ChatMessageEvent {
    private UUID receiverId;
    private UUID chatId;
    private String senderName;
    private String message;
    private ChatSessionDto session;
}
