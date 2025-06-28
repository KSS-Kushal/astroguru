package com.kss.astrologer.controllers;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kss.astrologer.dto.AstrologerDto;
import com.kss.astrologer.dto.WalletDto;
import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.request.AddBalanceRequest;
import com.kss.astrologer.request.AstrologerRequest;
import com.kss.astrologer.services.AdminService;
import com.kss.astrologer.services.AstrologerService;
import com.kss.astrologer.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin")
@Validated
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AstrologerService astrologerService;

    @Autowired
    private AdminService adminService;

    @GetMapping("/create-admin")
    public ResponseEntity<Object> createAdmin() {
        final String mobile = "9749610532";

        userService.getOrCreateAdmin(mobile);
        logger.info("Admin created Successfully!");
        return ResponseHandler.responseBuilder(HttpStatus.CREATED, true, "Admin created successfully");
    }

    @PostMapping("/astrologer")
    public ResponseEntity<Object> createAstrologer(@RequestBody @Valid AstrologerRequest astrologerRequest) {
        AstrologerDto astrologer = astrologerService.createAstrologer(astrologerRequest);
        logger.info("Astrologer created successfully: {}", astrologer.getUser().getMobile());
        return ResponseHandler.responseBuilder(HttpStatus.CREATED, true, "Astrologer created successfully", "astrologer", astrologer);
    }

    @DeleteMapping("/astrologer/{id}")
    public ResponseEntity<Object> deleteAstrologerById(@PathVariable UUID id) {
        AstrologerDto astrologer = astrologerService.deleteAstrologerById(id);
        logger.info("Deleted Astrologer with ID: {}", id);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Astrologer deleted successfully", "astrologer", astrologer);
    }

    @PostMapping("/add-balance")
    public ResponseEntity<Object> addBalanceInUserAccount(@RequestBody AddBalanceRequest request) {
        WalletDto wallet = adminService.addBalanceInUserWallet(request.getMobile(), request.getAmount());
        logger.info("transactions: " + wallet.getTransactions().size());
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Balance added successfully", "wallet", wallet);
    }
}
