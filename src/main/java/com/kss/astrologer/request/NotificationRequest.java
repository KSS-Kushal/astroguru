package com.kss.astrologer.request;

import com.kss.astrologer.types.NotificationCategory;
import com.kss.astrologer.types.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class NotificationRequest {
    private NotificationCategory category;
    private NotificationType type;

    private UUID userId;              // for DIRECT / CHAT
    private List<UUID> userIds;        // for BROADCAST

    private String title;
    private String message;
    private String actionUrl;

    private Map<String, Object> metadata;

    // Push config
    private boolean push;
    private boolean highPriority;
}
