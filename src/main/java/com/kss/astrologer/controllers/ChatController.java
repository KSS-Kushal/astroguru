package com.kss.astrologer.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kss.astrologer.dto.ChatMessageDto;
import com.kss.astrologer.dto.ChatSessionDto;
import com.kss.astrologer.dto.UserDto;
import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.request.ChatRequest;
import com.kss.astrologer.security.CustomUserDetails;
import com.kss.astrologer.services.ChatMessageService;
import com.kss.astrologer.services.ChatSessionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatMessageService chatMessageService;

    @PostMapping("/request")
    public ResponseEntity<Object> requestChat(@RequestBody @Valid ChatRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        System.out.println("User Id: " + userId);
        long position = chatSessionService.requestChat(userId, dto.getAstrologerId(), dto.getDuration());
        if (position == 0)
            return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Chat started");
        return ResponseHandler.responseBuilder(HttpStatus.OK, true,
                "Astrologer is busy. You are in queue at position: " + position);
    }

    @GetMapping("/accept/{userId}")
    public ResponseEntity<?> acceptChat(@PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID astrologerId = userDetails.getUserId();
        String msg = chatSessionService.acceptChat(userId, astrologerId);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, msg);
    }

    @GetMapping("/queue")
    public ResponseEntity<Object> getRequestList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID astrologerId = userDetails.getUserId();
        List<UserDto> users = chatSessionService.getRequestList(astrologerId);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Request list fetched successfully", "users",
                users);
    }

    @GetMapping("/remove-all")
    public ResponseEntity<Object> removeAllUserFromQueue(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID astrologerId = userDetails.getUserId();
        chatSessionService.removeAllUserFromQueue(astrologerId);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "All users removed from queue");
    }

    @GetMapping("/history")
    public ResponseEntity<Object> getChatHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size) {
        Page<ChatSessionDto> chatHistory = chatSessionService.getHistory(userDetails.getUserId(), page, size);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Chat history fetched successfully", "chatHistory",
                chatHistory);
    }

    @GetMapping("/messages/{sessionId}")
    public ResponseEntity<Object> getMessagesBySessionId(
            @PathVariable UUID sessionId,
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size) {
        Page<ChatMessageDto> messages = chatMessageService.getMessages(sessionId, page, size);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Messages fetched successfully", "messages",
                messages);
    }
}
