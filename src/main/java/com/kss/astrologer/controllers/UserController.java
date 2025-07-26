package com.kss.astrologer.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.kss.astrologer.dto.AstrologerDto;
import com.kss.astrologer.dto.UserDto;
import com.kss.astrologer.dto.WalletDto;
import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.models.User;
import com.kss.astrologer.models.Wallet;
import com.kss.astrologer.models.WalletTransaction;
import com.kss.astrologer.request.KundliRequest;
import com.kss.astrologer.security.CustomUserDetails;
import com.kss.astrologer.services.AstrologerService;
import com.kss.astrologer.services.UserService;
import com.kss.astrologer.services.WalletService;
import com.kss.astrologer.types.Role;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AstrologerService astrologerService;

    @Autowired
    private WalletService walletService;

    @GetMapping
    public ResponseEntity<Object> loginUserDetails(@AuthenticationPrincipal CustomUserDetails userDetails) {
        logger.info("Fetching user details for user: {}", userDetails.getUsername());
        if (userDetails.getRole() == Role.ASTROLOGER) {
            AstrologerDto astrologerDto = astrologerService.getAstrologerByUserId(userDetails.getUserId());
            return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Astrologer details fetched successfully",
                    "astrologer", astrologerDto);
        } else {
            UserDto userDto = userService.getUserById(userDetails.getUserId());
            return ResponseHandler.responseBuilder(HttpStatus.OK, true, "User details fetched successfully", "user",
                    userDto);
        }
    }

    @PostMapping
    public ResponseEntity<Object> addUserDetails(@AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody KundliRequest data) {
        logger.info("Adding user details for user: {}", userDetails.getUsername());
        // if(userDetails.getRole() == Role.ASTROLOGER) {
        // return ResponseHandler.responseBuilder(HttpStatus.FORBIDDEN, false,
        // "Astrologers cannot add user details", null, null);
        // }

        UserDto userDto = userService.addUserDetails(data, userDetails.getUserId());
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "User details added successfully", "user", userDto);
    }

    @GetMapping("/{mobile}")
    public ResponseEntity<Object> findUserByMobile(@PathVariable String mobile) {
        logger.info("Fetching user by mobile: {}", mobile);
        User user = userService.getUserByMobile(mobile);
        if (user == null) {
            return ResponseHandler.responseBuilder(HttpStatus.NOT_FOUND, false, "User not found");
        }
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "User found successfully", "user", user);
    }

    @GetMapping("/wallet")
    public ResponseEntity<Object> getWalletDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size) {
        Wallet wallet = walletService.getWalletByUserId(userDetails.getUserId());
        if (wallet == null) {
            return ResponseHandler.responseBuilder(HttpStatus.NOT_FOUND, false, "Wallet not found");
        }
        Page<WalletTransaction> transactions = walletService.getTransaction(wallet.getId(), page, size);
        wallet.setTransactions(transactions.getContent());
        WalletDto walletDto = new WalletDto(wallet);

        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Wallet details fetched successfully", "wallet",
                walletDto, transactions.getNumber() + 1, transactions.getTotalPages(), transactions.getTotalElements(),
                transactions.isLast());
    }

    @PostMapping(value = "/upload/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadProfileImage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @RequestPart("image") MultipartFile imgFile){
        UserDto userDto = userService.uploadProfileImage(userDetails.getUserId(), imgFile);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Uploaded Successfully", "user", userDto);
    }

}
