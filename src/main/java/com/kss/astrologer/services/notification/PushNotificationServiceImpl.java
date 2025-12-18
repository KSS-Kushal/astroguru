package com.kss.astrologer.services.notification;

import com.google.firebase.messaging.*;
import com.kss.astrologer.request.NotificationRequest;
import com.kss.astrologer.services.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService{
    private final DeviceTokenService deviceTokenService;

    private static final String CHANNEL_ID = "high_importance_channel";
    private static final String CUSTOM_SOUND = "notification_sound"; // raw file name

//    public void send(UUID userId, String title, String body, Map<String, Object> data) {
//
//        List<String> tokens = deviceTokenService.getTokens(userId);
//        if (tokens.isEmpty()) return;
//
//        MulticastMessage message = MulticastMessage.builder()
//                .addAllTokens(tokens)
//                .setNotification(
//                        Notification.builder()
//                                .setTitle(title)
//                                .setBody(body)
//                                .build()
//                )
//                .setAndroidConfig(AndroidConfig.builder()
//                        .setPriority(AndroidConfig.Priority.HIGH) // 🔥 High priority
//                        .setNotification(AndroidNotification.builder()
//                                .setChannelId("high_importance_channel") // 🔥 Channel must match app code
//                                .setSound("default") // Play default sound
//                                .setPriority(AndroidNotification.Priority.HIGH) // Heads-up
//                                .build())
//                        .build()
//                )
//                .putAllData(convertToStringMap(data))
//                .build();
//
//        try {
//            FirebaseMessaging.getInstance().sendEachForMulticast(message);
//        } catch (Exception e) {
//            log.error("FCM error", e);
//        }
//    }

    private Map<String, String> convertToStringMap(Map<String, Object> data) {
        Map<String, String> map = new HashMap<>();
        data.forEach((k, v) -> map.put(k, String.valueOf(v)));
        return map;
    }


    /* =====================================================
       DIRECT (Booking, Like, Comment)
       ===================================================== */
    @Override
    public void sendDirect(NotificationRequest req) {
        List<String> tokens = deviceTokenService.getTokens(req.getUserId());
        if (tokens.isEmpty()) return;


        sendMulticast(tokens, req, false);
    }

    /* =====================================================
       CHAT (Message + Reply button)
       ===================================================== */
    @Override
    public void sendChat(NotificationRequest req) {
        List<String> tokens = deviceTokenService.getTokens(req.getUserId());
        if (tokens.isEmpty()) return;

        // Help mobile app identify chat notification
        req.getMetadata().put("type", "CHAT");

        sendMulticast(tokens, req, true);
    }

    /* =====================================================
       BROADCAST (Post → All users)
       ===================================================== */
    @Override
    public void sendBroadcast(NotificationRequest req) {

        UUID excludeUserId = req.getUserId(); // post owner / sender

        List<String> tokens = deviceTokenService
                .getAllActiveTokensExceptUser(excludeUserId);

        if (tokens.isEmpty()) return;

        // FCM limit = 500 tokens
        List<List<String>> batches = partition(tokens, 300);

        for (List<String> batch : batches) {
            sendMulticast(batch, req, false);
        }
    }

    /* =====================================================
       SILENT (Background sync)
       ===================================================== */
    @Override
    public void sendSilent(NotificationRequest req) {

        List<String> tokens = deviceTokenService.getTokens(req.getUserId());
        if (tokens.isEmpty()) return;

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .putAllData(convertToStringMap(req.getMetadata()))
                    .setAndroidConfig(
                            AndroidConfig.builder()
                                    .setPriority(AndroidConfig.Priority.HIGH)
                                    .build()
                    )
                    .build();

            FirebaseMessaging.getInstance().sendEachForMulticast(message);

        } catch (Exception e) {
            log.error("Silent push failed", e);
        }
    }

    /* =====================================================
       CORE MULTICAST BUILDER
       ===================================================== */
    private void sendMulticast(List<String> tokens,
                               NotificationRequest req,
                               boolean isChat) {

        try {
            if (req.getActionUrl() != null) {
                req.getMetadata().put("actionUrl", req.getActionUrl());
            }
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)

                    // 🔔 Notification payload (Heads-up)
                    .setNotification(
                            Notification.builder()
                                    .setTitle(req.getTitle())
                                    .setBody(req.getMessage())
                                    .build()
                    )

                    // 🔥 Android config (HIGH priority + custom sound)
                    .setAndroidConfig(
                            AndroidConfig.builder()
                                    .setPriority(AndroidConfig.Priority.HIGH)
                                    .setNotification(
                                            AndroidNotification.builder()
                                                    .setChannelId(CHANNEL_ID)
                                                    .setSound(CUSTOM_SOUND)
                                                    .setPriority(AndroidNotification.Priority.HIGH)
                                                    .build()
                                    )
                                    .build()
                    )

                    // 📦 Data payload
                    .putAllData(convertToStringMap(req.getMetadata()))
                    .build();

            FirebaseMessaging.getInstance()
                    .sendEachForMulticast(message);

        } catch (Exception e) {
            log.error("FCM push failed", e);
        }
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return result;
    }
}
