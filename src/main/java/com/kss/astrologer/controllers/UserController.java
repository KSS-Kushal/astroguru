package com.kss.astrologer.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kss.astrologer.dto.AstrologerDto;
import com.kss.astrologer.dto.UserDto;
import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.request.KundliRequest;
import com.kss.astrologer.security.CustomUserDetails;
import com.kss.astrologer.services.AstrologerService;
import com.kss.astrologer.services.UserService;
import com.kss.astrologer.types.Role;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AstrologerService astrologerService;

    @GetMapping
    public ResponseEntity<Object> loginUserDetails(@AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Fetching user details for user: {}", userDetails.getUsername());
        if(userDetails.getRole() == Role.ASTROLOGER) {
            AstrologerDto astrologerDto = astrologerService.getAstrologerByUserId(userDetails.getUserId());
            return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Astrologer details fetched successfully", "astrologer", astrologerDto);
        } else {
            UserDto userDto = userService.getUserById(userDetails.getUserId());
            return ResponseHandler.responseBuilder(HttpStatus.OK, true, "User details fetched successfully", "user", userDto);
        }
    }

    @PostMapping
    public ResponseEntity<Object> addUserDetails(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody KundliRequest data) {
        logger.info("Adding user details for user: {}", userDetails.getUsername());
        // if(userDetails.getRole() == Role.ASTROLOGER) {
        //     return ResponseHandler.responseBuilder(HttpStatus.FORBIDDEN, false, "Astrologers cannot add user details", null, null);
        // }
        
        UserDto userDto = userService.addUserDetails(data, userDetails.getUserId());
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "User details added successfully", "user", userDto);
    }
}
