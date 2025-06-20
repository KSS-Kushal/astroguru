package com.kss.astrologer.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kss.astrologer.types.MessageType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatMessageDto {
    private UUID sessionId;
    private MessageType type; // "TEXT" or "IMAGE"
    private UUID senderId;
    private UUID receiverId;
    private String message; // message or base64 image URL
    private LocalDateTime timestamp;
}
