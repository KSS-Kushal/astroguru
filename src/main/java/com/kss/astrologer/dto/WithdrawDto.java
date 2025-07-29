package com.kss.astrologer.dto;

import com.kss.astrologer.models.User;
import com.kss.astrologer.models.WalletTransaction;
import com.kss.astrologer.models.Withdraw;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawDto {
    private UUID id;
    private UserDto user;
    private WalletTransaction walletTransaction;
    private Double amount;
    private Boolean isApproved;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime approvedAt = LocalDateTime.now();

    public WithdrawDto(Withdraw withdraw) {
        this.id = withdraw.getId();
        this.user = new UserDto(withdraw.getUser());
        this.walletTransaction = withdraw.getWalletTransaction();
        this.amount = withdraw.getAmount();
        this.isApproved = withdraw.getIsApproved();
        this.createdAt = withdraw.getCreatedAt();
        this.approvedAt = withdraw.getApprovedAt();
    }
}
