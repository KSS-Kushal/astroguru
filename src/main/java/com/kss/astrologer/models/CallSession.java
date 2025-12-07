package com.kss.astrologer.models;

import com.kss.astrologer.types.ChatStatus;
import com.kss.astrologer.types.SessionType;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "call_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallSession {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "astrologer_id")
    private User astrologer;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    @Enumerated(EnumType.STRING)
    private ChatStatus status; // ACTIVE, ENDED, CANCELLED

    @Enumerated(EnumType.STRING)
    private SessionType sessionType; // AUDIO / VIDEO

    private int totalMinutes;
    private Double totalCost;

    @OneToOne
    private BookingAppointment appointment;

    private String agoraChannelName;
    private String agoraToken;
}
