package com.kss.astrologer.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatQueueEntry {
    private UUID userId;
    private int requestedMinutes;
}
