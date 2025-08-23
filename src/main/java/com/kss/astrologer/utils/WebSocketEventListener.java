package com.kss.astrologer.utils;

import java.util.UUID;

import com.kss.astrologer.services.AstrologerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.kss.astrologer.services.OnlineUserService;

@Component
public class WebSocketEventListener {
//    @Autowired
//    private OnlineUserService onlineUserService;
//
//    @Autowired
//    private AstrologerService astrologerService;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userIdHeader = headerAccessor.getFirstNativeHeader("user-id");
        if (userIdHeader != null) {
            UUID userId = UUID.fromString(userIdHeader);
//            onlineUserService.addUser(userId);
//            onlineUserService.sendNotification();
//            astrologerService.sendOnlineAstrologer();
            logger.info("User connected: {}", userId);

            headerAccessor.getSessionAttributes().put("user-id", userId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//        String userIdHeader = headerAccessor.getFirstNativeHeader("user-id");
        Object userIdObj = headerAccessor.getSessionAttributes().get("user-id");
        if (userIdObj instanceof UUID userId) {
//            UUID userId = UUID.fromString(userIdHeader);
//            onlineUserService.removeUser(userId);
//            onlineUserService.sendNotification();
//            astrologerService.sendOnlineAstrologer();
            logger.info("User disconnected: {}", userId);
        } else {
            logger.warn("No user-id found in session during disconnect");
        }
    }
}
