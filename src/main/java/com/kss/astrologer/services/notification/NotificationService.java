package com.kss.astrologer.services.notification;

import com.kss.astrologer.models.Notification;
import com.kss.astrologer.request.NotificationRequest;
import com.kss.astrologer.types.NotificationType;
import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.UUID;

public interface NotificationService {
//    void sendNotification(UUID userId, String title, String message);
//    void sendNotification(
//            UUID userId,
//            NotificationType type,
//            String title,
//            String message,
//            String actionUrl,
//            Map<String, Object> metadata
//    );

    void sendNotification(NotificationRequest request);

    Page<Notification> getNotifications(UUID userId, Integer page, Integer size);
    long unreadCount(UUID userId);
    void markRead(UUID id);
}
