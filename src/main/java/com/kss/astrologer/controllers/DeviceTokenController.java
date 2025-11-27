package com.kss.astrologer.controllers;

import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.models.AstrologerDeviceToken;
import com.kss.astrologer.security.CustomUserDetails;
import com.kss.astrologer.services.DeviceTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/device-token")
public class DeviceTokenController {
    @Autowired
    private DeviceTokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<Object> registerToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> body) {
        String deviceToken = body.get("deviceToken");

        AstrologerDeviceToken astrologerDeviceToken = tokenService.saveOrUpdateToken(userDetails.getUserId(), deviceToken);

        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Token saved successfully", "astrologerDeviceToken", astrologerDeviceToken);
    }
}
