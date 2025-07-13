package com.kss.astrologer.dto;

import java.util.UUID;

import com.kss.astrologer.types.SessionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatQueueEntry {
    private UUID userId;
    private int requestedMinutes;
    private SessionType sessionType;
}
