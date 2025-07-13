package com.kss.astrologer.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kss.astrologer.models.ChatMessage;
import com.kss.astrologer.types.MessageType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private UUID sessionId;
    private MessageType type; // "CHAT" or "IMAGE"
    private UUID senderId;
    private UUID receiverId;
    private String message; // message or base64 image URL
    private LocalDateTime timestamp;

    public ChatMessageDto(ChatMessage chatMessage) {
        this.sessionId = chatMessage.getSession().getId();
        this.type = chatMessage.getMessageType();
        this.senderId = chatMessage.getSender().getId();
        this.receiverId = chatMessage.getReceiver().getId();
        this.message = chatMessage.getMessage();
        this.timestamp = chatMessage.getCreatedAt();
    }
}
