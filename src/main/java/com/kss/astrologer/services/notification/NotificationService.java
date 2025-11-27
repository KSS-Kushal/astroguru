package com.kss.astrologer.services.notification;

import java.util.UUID;

public interface NotificationService {
    public void sendNotification(UUID userId, String title, String message);
}
