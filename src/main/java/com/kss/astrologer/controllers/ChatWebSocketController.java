package com.kss.astrologer.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.kss.astrologer.dto.*;
import com.kss.astrologer.events.BookingRequestEvent;
import com.kss.astrologer.events.ChatMessageEvent;
import com.kss.astrologer.models.Notification;
import com.kss.astrologer.repository.NotificationRepository;
import com.kss.astrologer.request.ActiveSessionRequest;
import com.kss.astrologer.request.CallEnd;
import com.kss.astrologer.request.ChatLeave;
import com.kss.astrologer.services.*;
import com.kss.astrologer.services.notification.NotificationService;
import com.kss.astrologer.types.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.kss.astrologer.models.ChatMessage;
import com.kss.astrologer.models.ChatSession;
import com.kss.astrologer.models.User;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private CallSessionService callSessionService;

    @Autowired
    private ChatQueueService chatQueueService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private OnlineUserService onlineUserService;

//    @Autowired
//    private NotificationService notificationService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private NotificationRepository notificationRepository;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDto dto) {
        // Convert DTO to entity
        ChatSession session = chatSessionService.getSessionById(dto.getSessionId());
        User sender = userService.getById(dto.getSenderId());
        User receiver = userService.getById(dto.getReceiverId());

        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setMessage(dto.getMessage());
        message.setMessageType(dto.getType());
        message.setCreatedAt(LocalDateTime.now());

        // Persist
        ChatMessage saved = chatMessageService.save(message);

        ChatMessageDto chatMessageDto = new ChatMessageDto(saved);
        // Send to receiver
        messagingTemplate.convertAndSend("/topic/chat/" + chatMessageDto.getReceiverId() + "/messages", chatMessageDto);

        if (!onlineUserService.isOnline(chatMessageDto.getReceiverId())) {
            Map<String, Object> map = new HashMap<>();
            map.put("chatId", chatMessageDto.getId());
            map.put("sender", saved.getSender().getName());
            map.put("type", NotificationType.CHAT_MESSAGE);
            map.put("session", new ChatSessionDto(session));
            Notification notification = Notification.builder()
                    .userId(chatMessageDto.getReceiverId())
                    .type(NotificationType.CHAT_MESSAGE)
                    .title(saved.getSender().getName())
                    .message(chatMessageDto.getMessage())
                    .actionUrl("/chat/" + chatMessageDto.getId())
                    .metadata(map)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }
        eventPublisher.publishEvent(
                new ChatMessageEvent(
                        chatMessageDto.getReceiverId(),
                        chatMessageDto.getId(),
                        saved.getSender().getName(),
                        chatMessageDto.getMessage(),
                        new ChatSessionDto(session)
                )
        );
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingIndicator typingIndicator) {
        messagingTemplate.convertAndSend("/topic/chat/" + typingIndicator.getReceiverId() + "/typing", typingIndicator);
    }

    @MessageMapping("/chat.leave")
    public void userLeave(@Payload ChatLeave chatLeave) {
        chatQueueService.removeUser(chatLeave.getAstrologerId(), chatLeave.getUserId());
        QueueNotificationDto notificationDtoForUser = new QueueNotificationDto(chatLeave.getUserId(), chatLeave.getSessionType(), "Exited from waiting list");
        QueueNotificationDto notificationDtoForAstrologer = new QueueNotificationDto(chatLeave.getUserId(), chatLeave.getSessionType(), "One user exited from waiting list");
        messagingTemplate.convertAndSend("/topic/queue/" + chatLeave.getUserId(), notificationDtoForUser);
        messagingTemplate.convertAndSend("/topic/queue/" + chatLeave.getAstrologerId(), notificationDtoForAstrologer);

        List<QueueEntryDto> requests = chatSessionService.getRequestList(chatLeave.getAstrologerId());
        messagingTemplate.convertAndSend("/topic/requests/" + chatLeave.getAstrologerId(), requests);
//        notificationService.sendNotification(chatLeave.getAstrologerId(), chatLeave.getSessionType() + " Request Canceled", "Someone has canceled a "+ chatLeave.getSessionType() +" request.");

    }

    @MessageMapping("/call.end")
    public void endCallByUser(@Payload CallEnd callEnd) {
        callSessionService.endCallByUser(callEnd.getSessionId());
    }

    @MessageMapping("/session.active")
    public void getActiveSession(@Payload ActiveSessionRequest activeSessionRequest) {
        UUID astrologerId = activeSessionRequest.getAstrologerId();
        ChatSessionDto chatSession = chatSessionService.getActiveSession(astrologerId);
        if (chatSession != null)
            messagingTemplate.convertAndSend("/topic/session/" + astrologerId, chatSession);
        CallSessionDto callSession = callSessionService.getActiveSession(astrologerId);
        if (callSession != null)
            messagingTemplate.convertAndSend("/topic/session/" + astrologerId, callSession);
    }

    @MessageMapping("/online.user")
    public void getOnlineAstrologers() {
        onlineUserService.sendNotification();
    }

}
