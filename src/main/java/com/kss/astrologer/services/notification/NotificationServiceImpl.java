package com.kss.astrologer.services.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kss.astrologer.models.Notification;
import com.kss.astrologer.repository.NotificationRepository;
import com.kss.astrologer.request.NotificationRequest;
import com.kss.astrologer.services.DeviceTokenService;
import com.kss.astrologer.types.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private DeviceTokenService deviceTokenService;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private PushNotificationService pushService;
    @Autowired
    private ObjectMapper objectMapper;

//    @Override
//    public void sendNotification(
//            UUID userId,
//            NotificationType type,
//            String title,
//            String message,
//            String actionUrl,
//            Map<String, Object> metadata
//    ) {
//        try {
//            Notification notification = Notification.builder()
//                    .userId(userId)
//                    .type(type)
//                    .title(title)
//                    .message(message)
//                    .actionUrl(actionUrl)
//                    .metadata(objectMapper.writeValueAsString(metadata))
//                    .isRead(false)
//                    .build();
//
//            notificationRepository.save(notification);
//
//            pushService.send(userId, title, message, metadata);
//
//        } catch (Exception e) {
//            logger.error("Notification error", e);
//        }
//    }

    @Override
    public void sendNotification(NotificationRequest request) {

        switch (request.getCategory()) {

            case DIRECT -> handleDirect(request);

            case CHAT -> handleChat(request);

            case BROADCAST -> handleBroadcast(request);

            case SILENT -> handleSilent(request);
        }
    }

    /* ---------------- DIRECT ---------------- */

    private void handleDirect(NotificationRequest req) {
        save(req.getUserId(), req);
        if (req.isPush()) {
            pushService.sendDirect(req);
        }
    }

    /* ---------------- CHAT ---------------- */

    private void handleChat(NotificationRequest req) {
        save(req.getUserId(), req);

        // Push only if app closed
        if (req.isPush()) {
            pushService.sendChat(req);
        }
    }

    /* ---------------- BROADCAST ---------------- */

    private void handleBroadcast(NotificationRequest req) {
        for (UUID userId : req.getUserIds()) {
            if (!userId.equals(req.getUserId())) save(userId, req);
        }

        // Push in batches
        pushService.sendBroadcast(req);
    }

    /* ---------------- SILENT ---------------- */

    private void handleSilent(NotificationRequest req) {
        // No DB entry
        pushService.sendSilent(req);
    }

    /* ---------------- SAVE ---------------- */

    private void save(UUID userId, NotificationRequest req) {
        try {
            Notification notification = Notification.builder()
                    .userId(userId)
                    .type(req.getType())
                    .title(req.getTitle())
                    .message(req.getMessage())
                    .actionUrl(req.getActionUrl())
                    .metadata(req.getMetadata())
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);

        } catch (Exception e) {
            logger.error("Notification save error", e);
        }
    }

    @Override
    public Page<Notification> getNotifications(UUID userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public long unreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markRead(UUID id) {
        Notification n = notificationRepository.findById(id).orElseThrow();
        n.setRead(true);
        notificationRepository.save(n);
    }
}
