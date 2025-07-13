package com.kss.astrologer.controllers;

import java.util.UUID;

import com.kss.astrologer.models.Bannar;
import com.kss.astrologer.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kss.astrologer.dto.AstrologerDto;
import com.kss.astrologer.dto.WalletDto;
import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.models.Wallet;
import com.kss.astrologer.models.WalletTransaction;
import com.kss.astrologer.request.AddBalanceRequest;
import com.kss.astrologer.request.AstrologerRequest;
import com.kss.astrologer.services.aws.S3Service;

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

    @Autowired
    private WalletService walletService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private BannarService bannarService;

    @GetMapping("/create-admin")
    public ResponseEntity<Object> createAdmin() {
        final String mobile = "9749610532";

        userService.getOrCreateAdmin(mobile);
        logger.info("Admin created Successfully!");
        return ResponseHandler.responseBuilder(HttpStatus.CREATED, true, "Admin created successfully");
    }

    @PostMapping(value = "/astrologer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createAstrologer(
        @RequestPart("data") @Valid AstrologerRequest astrologerRequest,
        @RequestPart("image") MultipartFile imageFile) {
            logger.info("running create astrologer");
        String imgUrl = s3Service.uploadFile(imageFile, "astrologers");
        logger.info("imgUrl = " + imgUrl);
        AstrologerDto astrologer = astrologerService.createAstrologer(astrologerRequest, imgUrl);
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

    @GetMapping("/wallet/{userId}")
    public ResponseEntity<Object> getWalletDetails(
            @PathVariable UUID userId,
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size) {
        Wallet wallet = walletService.getWalletByUserId(userId);
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

    @PostMapping(value = "upload-bannar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadBannar(@RequestPart("image") MultipartFile imageFile) {
        Bannar bannar = bannarService.uploadBannar(imageFile);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Bannar Uploaded Successfully", "bannar", bannar);
    }

    @DeleteMapping("/bannar/{id}")
    public ResponseEntity<Object> deleteBannar(@PathVariable UUID id) {
        Bannar bannar = bannarService.deleteBannar(id);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Bannar Deleted Successfully", "bannar", bannar);
    }
}
