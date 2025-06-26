package com.kss.astrologer.controllers;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.request.ChatRequest;
import com.kss.astrologer.security.CustomUserDetails;
import com.kss.astrologer.services.ChatSessionService;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    private ChatSessionService chatSessionService;

    @PostMapping("/request")
    public ResponseEntity<?> requestChat(@RequestBody ChatRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        long position = chatSessionService.requestChat(userId, dto.getAstrologerId(), dto.getDuration());
        if(position == 0)
                return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Chat started");
        return ResponseHandler.responseBuilder(HttpStatus.OK, true,
                "Astrologer is busy. You are in queue at position: " + position);
    }

    @GetMapping("/accept/{userId}")
    public ResponseEntity<?> acceptChat(@PathVariable UUID userId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID astrologerId = userDetails.getUserId();
        String msg = chatSessionService.acceptChat(userId, astrologerId);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, msg);
    }
}
