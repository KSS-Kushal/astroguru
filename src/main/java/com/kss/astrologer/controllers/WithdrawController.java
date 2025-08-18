package com.kss.astrologer.controllers;

import com.kss.astrologer.dto.WithdrawDto;
import com.kss.astrologer.exceptions.CustomException;
import com.kss.astrologer.handler.ResponseHandler;
import com.kss.astrologer.security.CustomUserDetails;
import com.kss.astrologer.services.WithdrawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/withdraw")
public class WithdrawController {

    @Autowired
    private WithdrawService withdrawService;

    @GetMapping("/request")
    public ResponseEntity<Object> withdrawRequest(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @RequestParam(required = false, defaultValue = "1.0") Double amount) {
        if(amount.isNaN()) throw new CustomException("Invalid amount");
        WithdrawDto withdraw = withdrawService.createWithdrawRequest(amount, userDetails.getUserId());
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Withdraw requested successfully", "withdraw", withdraw);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/approve/{id}")
    public ResponseEntity<Object> approveRequest(@PathVariable UUID id) {
        WithdrawDto withdraw = withdrawService.approvedWithdrawRequest(id);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Withdraw request approved successfully", "withdraw", withdraw);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<Object> getAllWithdrawRequest(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        Page<WithdrawDto> withdraws = withdrawService.getWithdrawRequest(page, size);
        return ResponseHandler.responseBuilder(HttpStatus.OK, true, "Withdraw request fetched successfully", "withdraw", withdraws);
    }
}
