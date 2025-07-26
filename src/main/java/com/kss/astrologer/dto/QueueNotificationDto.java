package com.kss.astrologer.dto;

import com.kss.astrologer.types.SessionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueueNotificationDto {
    private UUID userId;
    private SessionType type;
    private String msg;
}
