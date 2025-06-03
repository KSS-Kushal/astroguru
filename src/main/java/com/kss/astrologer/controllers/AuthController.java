package com.kss.astrologer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kss.astrologer.dto.UserDto;
import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.models.User;
import com.kss.astrologer.request.AuthRequest;
import com.kss.astrologer.request.VerifyOtpRequest;
import com.kss.astrologer.security.JWTService;
import com.kss.astrologer.services.OtpService;
import com.kss.astrologer.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody @Valid AuthRequest authRequest) {
        String otp = otpService.sendOtp(authRequest.getMobile());
        return ResponseHandler.responseBuilder(HttpStatus.OK, true,  "OTP sent successfully", "otp", otp);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Object> verifyOtp(@RequestBody @Valid VerifyOtpRequest verifyOtpRequest) {
        if (otpService.verifyOtp(verifyOtpRequest.getMobile(), verifyOtpRequest.getOtp())) {
            User user = userService.getOrCreateUser(verifyOtpRequest.getMobile());
            String token = jwtService.generateToken(user.getMobile());
            UserDto userDto = new UserDto(user);
            return ResponseHandler.responseBuilder(HttpStatus.OK, true, "OTP verified successfully", "user", userDto, token);
        }

        return ResponseHandler.responseBuilder(HttpStatus.UNAUTHORIZED, false, "Invalid OTP");
    }
}
