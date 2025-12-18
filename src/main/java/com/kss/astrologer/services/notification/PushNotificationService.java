package com.kss.astrologer.services.notification;


import com.kss.astrologer.request.NotificationRequest;

public interface PushNotificationService {
    void sendDirect(NotificationRequest request);

    void sendChat(NotificationRequest request);

    void sendBroadcast(NotificationRequest request);

    void sendSilent(NotificationRequest request);
}
