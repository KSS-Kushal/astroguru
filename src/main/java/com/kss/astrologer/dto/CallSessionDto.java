package com.kss.astrologer.dto;

import com.kss.astrologer.models.CallSession;
import com.kss.astrologer.types.ChatStatus;
import com.kss.astrologer.types.SessionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallSessionDto {
    private UUID id;
    private UserDto user;
    private UserDto astrologer;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private ChatStatus status;
    private SessionType sessionType;
    private int totalMinutes;
    private Double totalCost;
    private String agoraChannelName;
    private String agoraToken;

    public CallSessionDto(CallSession session) {
        this.id = session.getId();
        this.user = session.getUser() != null ? new UserDto(session.getUser()) : null;
        this.astrologer = session.getAstrologer() != null ? new UserDto(session.getAstrologer()) : null;
        this.startedAt = session.getStartedAt();
        this.endedAt = session.getEndedAt();
        this.status = session.getStatus();
        this.sessionType = session.getSessionType();
        this.totalMinutes = session.getTotalMinutes();
        this.totalCost = session.getTotalCost();
        this.agoraChannelName = session.getAgoraChannelName();
        this.agoraToken = session.getAgoraToken();
    }
}
