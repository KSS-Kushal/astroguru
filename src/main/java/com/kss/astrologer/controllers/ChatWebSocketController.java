package com.kss.astrologer.controllers;

import java.time.LocalDateTime;

import com.kss.astrologer.request.ChatLeave;
import com.kss.astrologer.services.ChatQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.kss.astrologer.dto.ChatMessageDto;
import com.kss.astrologer.dto.TypingIndicator;
import com.kss.astrologer.models.ChatMessage;
import com.kss.astrologer.models.ChatSession;
import com.kss.astrologer.models.User;
import com.kss.astrologer.services.ChatMessageService;
import com.kss.astrologer.services.ChatSessionService;
import com.kss.astrologer.services.UserService;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatQueueService chatQueueService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatMessageService chatMessageService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDto dto) {
        // Convert DTO to entity
        System.out.println(dto);
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
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingIndicator typingIndicator) {
        messagingTemplate.convertAndSend("/topic/chat/" + typingIndicator.getReceiverId() + "/typing", typingIndicator);
    }

    @MessageMapping("/chat.leave")
    public void userLeave(@Payload ChatLeave chatLeave) {
        chatQueueService.removeUser(chatLeave.getAstrologerId(), chatLeave.getUserId());
        messagingTemplate.convertAndSend("/topic/queue/" + chatLeave.getUserId(), "Exited from waiting list");
        messagingTemplate.convertAndSend("/topic/queue/" + chatLeave.getAstrologerId(), "One user exited from waiting list");
    }

}
