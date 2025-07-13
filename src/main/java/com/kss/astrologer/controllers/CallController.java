package com.kss.astrologer.controllers;

import com.kss.astrologer.dto.CallSessionDto;
import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.request.CallRequest;
import com.kss.astrologer.security.CustomUserDetails;
import com.kss.astrologer.services.CallSessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/call")
public class CallController {

    @Autowired
    private CallSessionService callSessionService;

    @PostMapping("/request")
    public ResponseEntity<Object> requestCall(@RequestBody @Valid CallRequest dto,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        long position = callSessionService.requestCall(userId, dto.getAstrologerId(), dto.getDuration(), dto.getType());
        if (position == 0)
            return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Chat started");
        return ResponseHandler.responseBuilder(HttpStatus.OK, true,
                "Astrologer is busy. You are in queue at position: " + position);
    }

    @GetMapping("/accept/{userId}")
    public ResponseEntity<?> acceptCall(@PathVariable UUID userId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID astrologerId = userDetails.getUserId();
        String msg = callSessionService.acceptCall(userId, astrologerId);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, msg);
    }

    @GetMapping("/history")
    public ResponseEntity<Object> getCallHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size) {
        Page<CallSessionDto> callHistory = callSessionService.getHistory(userDetails.getUserId(), page, size);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Chat history fetched successfully", "chatHistory",
                callHistory);
    }
}
