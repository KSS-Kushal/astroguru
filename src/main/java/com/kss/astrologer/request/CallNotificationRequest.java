package com.kss.astrologer.request;

import lombok.Data;

import java.util.UUID;

@Data
public class CallNotificationRequest {
    private UUID receiverId;
    private String sessionType;
}
