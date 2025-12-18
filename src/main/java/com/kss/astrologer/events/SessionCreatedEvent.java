package com.kss.astrologer.events;

import com.kss.astrologer.types.SessionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class SessionCreatedEvent {
    private UUID userId;
    private UUID astrologerId;
    private UUID sessionId;
    private SessionType sessionType;

}
