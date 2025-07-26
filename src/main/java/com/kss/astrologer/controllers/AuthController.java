package com.kss.astrologer.controllers;

import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.types.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Object> login(@RequestBody @Valid AuthRequest authRequest,
                                        @RequestParam(required = false, defaultValue = "USER") Role role) {
        if(role == Role.ADMIN) {
            User user = userService.getUserByMobile(authRequest.getMobile());
            if(user==null) throw new CustomException(HttpStatus.NOT_FOUND, "Admin not found");
            if(user.getRole()!=Role.ADMIN) throw new CustomException(HttpStatus.FORBIDDEN, "You have not Admin access");
        }
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
