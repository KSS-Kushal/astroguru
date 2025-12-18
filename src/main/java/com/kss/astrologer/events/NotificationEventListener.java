package com.kss.astrologer.events;

import com.kss.astrologer.request.NotificationRequest;
import com.kss.astrologer.services.notification.NotificationService;
import com.kss.astrologer.types.NotificationCategory;
import com.kss.astrologer.types.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {
    private final NotificationService notificationService;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingRequested(BookingRequestEvent event) {

        log.info("BookingRequestedEvent for booking {}", event.getBookingId());

        Map<String, Object> map = new HashMap<>();
        map.put("bookingId", event.getBookingId());
        map.put("userId", event.getUserId());
        NotificationRequest notificationRequest = NotificationRequest.builder()
                        .userId(event.getAstrologerId())
                        .category(NotificationCategory.DIRECT)
                        .type(NotificationType.BOOKING_REQUEST)
                        .title("New Booking Request")
                        .message("You have received a new appointment request")
                        .actionUrl("/booking/" + event.getBookingId())
                        .metadata(map)
                        .push(true)
                        .highPriority(true)
                        .build();
        notificationService.sendNotification(notificationRequest);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingApproved(BookingApprovedEvent event) {

        log.info("BookingApprovedEvent for booking {}", event.getBookingId());

        Map<String, Object> map = new HashMap<>();
        map.put("bookingId", event.getBookingId());
        map.put("userId", event.getUserId());
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(event.getUserId())
                .category(NotificationCategory.DIRECT)
                .type(NotificationType.BOOKING_APPROVED)
                .title("Booking Approved")
                .message("Your appointment has been approved")
                .actionUrl("/booking/" + event.getBookingId())
                .metadata(map)
                .push(true)
                .highPriority(true)
                .build();

        notificationService.sendNotification(notificationRequest);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingCancelled(BookingCancelledEvent event) {

        log.info("BookingCancelledEvent for booking {}", event.getBookingId());

        Map<String, Object> map = new HashMap<>();
        map.put("bookingId", event.getBookingId());
        map.put("userId", event.getUserId());
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(event.getUserId())
                .category(NotificationCategory.DIRECT)
                .type(NotificationType.BOOKING_CANCELLED)
                .title("Booking Cancelled")
                .message("Your appointment has been cancelled")
                .actionUrl("/booking/" + event.getBookingId())
                .metadata(map)
                .push(true)
                .highPriority(true)
                .build();

        notificationService.sendNotification(notificationRequest);
    }

    /* =========================================================
       POST EVENTS
       ========================================================= */

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostCreated(PostCreatedEvent event) {

        log.info("PostCreatedEvent for post {}", event.getPostId());

        // Usually broadcast → followers
        // Here you can loop followers and send notification

        Map<String, Object> map = new HashMap<>();
        map.put("postId", event.getPostId());
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(event.getUserId())
                .category(NotificationCategory.BROADCAST)
                .type(NotificationType.POST_CREATED)
                .title("New Post")
                .message("New post published")
                .actionUrl("/post/" + event.getPostId())
                .metadata(map)
                .push(true)
                .highPriority(true)
                .build();

        notificationService.sendNotification(notificationRequest);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostLiked(PostLikedEvent event) {

        log.info("PostLikedEvent for post {}", event.getPostId());

        Map<String, Object> map = new HashMap<>();
        map.put("postId", event.getPostId());
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(event.getAstrologerId())
                .category(NotificationCategory.DIRECT)
                .type(NotificationType.POST_LIKED)
                .title("Post Liked ❤️")
                .message(event.getLikedUserName() + " liked your post")
                .actionUrl("/post/" + event.getPostId())
                .metadata(map)
                .push(true)
                .highPriority(true)
                .build();

        notificationService.sendNotification(notificationRequest);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostCommented(PostCommentedEvent event) {

        log.info("PostCommentedEvent for post {}", event.getPostId());

        Map<String, Object> map = new HashMap<>();
        map.put("postId", event.getPostId());
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(event.getAstrologerId())
                .category(NotificationCategory.DIRECT)
                .type(NotificationType.POST_COMMENTED)
                .title("New Comment 💬")
                .message(event.getCommenterUserName() + ": " + event.getComment())
                .actionUrl("/post/" + event.getPostId())
                .metadata(map)
                .push(true)
                .highPriority(true)
                .build();

        notificationService.sendNotification(notificationRequest);
    }

    /* =========================================================
       CHAT EVENTS
       ========================================================= */

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChatMessage(ChatMessageEvent event) {

        log.info("ChatMessageEvent for chat {}", event.getChatId());

        Map<String, Object> map = new HashMap<>();
        map.put("chatId", event.getChatId());
        map.put("sender", event.getSenderName());
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(event.getReceiverId())
                .category(NotificationCategory.DIRECT)
                .type(NotificationType.CHAT_MESSAGE)
                .title(event.getSenderName())
                .message(event.getMessage())
                .actionUrl("/chat/" + event.getChatId())
                .metadata(map)
                .push(true)
                .highPriority(true)
                .build();

        notificationService.sendNotification(notificationRequest);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSessionCreated(SessionCreatedEvent event) {

        log.info("SessionCreatedEvent for {}", event.getSessionId());

        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", event.getSessionId());
        map.put("sessionType", event.getSessionType());
        map.put("userId", event.getUserId());
        map.put("astrologerId", event.getUserId());
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(event.getUserId())
                .category(NotificationCategory.DIRECT)
                .type(NotificationType.SESSION_CREATED)
                .title(event.getSessionType() + " Session Created")
                .message("Your Session is created please join")
                .actionUrl("/session/" + event.getSessionId())
                .metadata(map)
                .push(true)
                .highPriority(true)
                .build();

        notificationService.sendNotification(notificationRequest);
    }
}
