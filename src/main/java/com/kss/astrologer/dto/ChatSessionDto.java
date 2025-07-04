package com.kss.astrologer.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.kss.astrologer.models.ChatSession;
import com.kss.astrologer.types.ChatStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionDto {
    private UUID id;
    private UserDto user;
    private UserDto astrologer;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private ChatStatus status;
    private int totalMinutes;
    private Double totalCost;

    public ChatSessionDto(ChatSession session) {
        this.id = session.getId();
        this.user = session.getUser() != null ? new UserDto(session.getUser()) : null;
        this.astrologer = session.getAstrologer() != null ? new UserDto(session.getAstrologer()) : null;
        this.startedAt = session.getStartedAt();
        this.endedAt = session.getEndedAt();
        this.status = session.getStatus();
        this.totalMinutes = session.getTotalMinutes();
        this.totalCost = session.getTotalCost();
    }
}
