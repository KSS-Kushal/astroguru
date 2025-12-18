package com.kss.astrologer.controllers;

import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.models.Notification;
import com.kss.astrologer.security.CustomUserDetails;
import com.kss.astrologer.services.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    @Autowired
    NotificationService notificationService;

    @GetMapping()
    ResponseEntity<Object> getAllNotification(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @RequestParam(defaultValue = "1", required = false) Integer page,
                                              @RequestParam(defaultValue = "10", required = false) Integer size) {
        Page<Notification> notifications = notificationService.getNotifications(userDetails.getUserId(), page, size);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Notification fetched successfully",
                "notifications", notifications);
    }

    @GetMapping("/count")
    ResponseEntity<Object> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        long count = notificationService.unreadCount(userDetails.getUserId());
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Notification count fetched successfully",
                "count", count);
    }

    @GetMapping("/read/{id}")
    ResponseEntity<Object> markRead(@PathVariable("id")UUID id) {
        notificationService.markRead(id);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Notification mark read successfully");
    }
}
