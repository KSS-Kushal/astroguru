package com.kss.astrologer.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.kss.astrologer.types.ChatStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
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

    private int totalMinutes;

    private Double totalCost;

    @OneToOne
    private BookingAppointment appointment;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<ChatMessage> messages;
}
