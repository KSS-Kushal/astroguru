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
    private UUID userId;
    private UUID astrologerId;
    private LocalDateTime startedAt;
    private ChatStatus status;
    private int totalMinutes;
    private Double totalCost;

    public ChatSessionDto(ChatSession session) {
        this.id = session.getId();
        this.userId = session.getUser() != null ? session.getUser().getId() : null;
        this.astrologerId = session.getAstrologer() != null ? session.getAstrologer().getId() : null;
        this.startedAt = session.getStartedAt();
        this.status = session.getStatus();
        this.totalMinutes = session.getTotalMinutes();
        this.totalCost = session.getTotalCost();
    }
}
