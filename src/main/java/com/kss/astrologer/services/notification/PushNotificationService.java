package com.kss.astrologer.services.notification;

import com.google.firebase.messaging.*;
import com.kss.astrologer.services.DeviceTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PushNotificationService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

    @Autowired
    private DeviceTokenService deviceTokenService;

    @Override
    public void sendNotification(UUID userId, String title, String message) {
        List<String> tokens = deviceTokenService.getTokens(userId);

        for (String token : tokens) {
            Message firebaseMessage = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(message)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH) // 🔥 High priority
                            .setNotification(AndroidNotification.builder()
                                    .setChannelId("high_importance_channel") // 🔥 Channel must match app code
                                    .setSound("default") // Play default sound
                                    .setPriority(AndroidNotification.Priority.HIGH) // Heads-up
                                    .build())
                            .build())
                    .build();

            try {
                String response = FirebaseMessaging.getInstance().send(firebaseMessage);
                logger.info("Notification sent successfully: {}", response);
            } catch (FirebaseMessagingException e) {
                logger.error("Error sending notification: {}", e.getMessage());
                // if error is "NotRegistered" → token expired, remove from DB
                deviceTokenService.deleteToken(token);
            }
        }
    }
}
