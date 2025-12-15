package com.kss.astrologer.controllers;

import com.kss.astrologer.dto.AstrologerDto;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.request.AuthWithPasswordRequest;
import com.kss.astrologer.request.RegisterAuthRequest;
import com.kss.astrologer.security.CustomUserDetails;
import com.kss.astrologer.services.AstrologerService;
import com.kss.astrologer.types.Role;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

@Tag(name = "Auth", description = "Authentication")
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

    @Autowired
    private AstrologerService astrologerService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody @Valid AuthRequest authRequest,
                                        @RequestParam(required = false, defaultValue = "USER") Role role) {
        if (role == Role.ADMIN) {
            User user = userService.getUserByMobile(authRequest.getMobile());
            if (user == null) throw new CustomException(HttpStatus.NOT_FOUND, "Admin not found");
            if (user.getRole() != Role.ADMIN)
                throw new CustomException(HttpStatus.FORBIDDEN, "You have not Admin access");
        }
        String otp = otpService.sendOtp(authRequest.getMobile());
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "OTP sent successfully", "otp", otp);
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

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody @Valid RegisterAuthRequest authRequest) {
        UserDto user = userService.registerUser(authRequest);
        String token = jwtService.generateToken(user.getMobile());
        return ResponseHandler.responseBuilder(HttpStatus.CREATED, true, "User registered successfully", "user", user, token);
    }

    @PostMapping("/login-by-password")
    public ResponseEntity<Object> loginWithPassword(@RequestBody @Valid AuthWithPasswordRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getMobile(), authRequest.getPassword())
            );
            if (authentication.isAuthenticated()) {
                User user = userService.getUserByMobile(authRequest.getMobile());
                UserDto userDto = new UserDto(user);
                String token = jwtService.generateToken(user.getMobile());
                return ResponseHandler.responseBuilder(HttpStatus.OK, true, "User logged in successfully", "user", userDto, token);
            }
            return ResponseHandler.responseBuilder(HttpStatus.UNAUTHORIZED, false, "Invalid credentials");
        } catch (BadCredentialsException e) {
            return ResponseHandler.responseBuilder(HttpStatus.UNAUTHORIZED, false, "Invalid credentials");
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<Object> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if(userDetails.getRole() == Role.ASTROLOGER) {
            AstrologerDto astrologer = astrologerService.logoutAstrologer(userDetails.getUserId());
            return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Successfully Logged Out!", "astrologer", astrologer);
        }
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Successfully Logged Out!");
    }
}
